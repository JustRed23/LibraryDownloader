package dev.JustRed23.LibraryDownloader;

import com.google.gson.*;
import dev.JustRed23.LibraryDownloader.error.LibraryDownloadException;
import dev.JustRed23.LibraryDownloader.utils.MD5;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

public final class LibraryDownloader {

    private final FileWriter logWriter;
    private final File LIBRARIES_FOLDER, LIBRARIES_JSON;

    private List<Library> libraries, toDownload, toRemove;

    public LibraryDownloader(File libraries_folder, File libraries_json) throws IOException {
        this.LIBRARIES_FOLDER = libraries_folder;
        this.LIBRARIES_JSON = libraries_json;

        libraries = new ArrayList<>();
        toDownload = new ArrayList<>();
        toRemove = new ArrayList<>();

        File log = new File("logs/LibraryDownloader.log");
        if (log.exists())
            log.delete();
        log.getParentFile().mkdirs();
        log.createNewFile();

        logWriter = new FileWriter(log, true);
    }

    public void start() throws Exception {
        boolean needs_update = librariesNeedUpdate();

        if(needs_update)
            updateLibraries();
    }

    private void updateLibraries() throws IOException, LibraryDownloadException {
        toRemove.forEach(libToRemove -> {
            File lib = new File(LIBRARIES_FOLDER, libToRemove.getPath());
            if (lib.exists())
                lib.delete();
        });

        for (Library library : toDownload) {
            File lib = new File(LIBRARIES_FOLDER, library.getPath() + library.getFileName());
            downloadLibrary(library, lib, 0);
        }
    }

    private boolean librariesNeedUpdate() throws Exception {
        log("Checking libraries, please wait...");
        if (!LIBRARIES_FOLDER.exists()) {
            log("Libraries folder does not exist, creating...");
            LIBRARIES_FOLDER.mkdir();
        }

        if (!LIBRARIES_JSON.exists()) {
            error("The libraries json file does not exist.");
            throw new FileNotFoundException("The libraries json file does not exist.");
        }

        JsonArray result = parseLibrariesJson(new BufferedReader(new FileReader(LIBRARIES_JSON)));

        ProgressBarBuilder builder = new ProgressBarBuilder()
                .setTaskName("Checking libraries...")
                .setUpdateIntervalMillis(100)
                .setStyle(ProgressBarStyle.ASCII);

        ProgressBar.wrap(StreamSupport.stream(result.spliterator(), true).parallel(), builder).forEach(jsonElement -> {
            try {
                Library newest = decode(jsonElement.getAsJsonObject()); //Get the new library from the json object and turn it into a library object
                Library foundOnPC = getLibraryFromPC(newest); //Get the library from the PC if it exists

                if (foundOnPC == null) {
                    System.out.println("Library '" + newest.getFileName() + "' not found on PC, adding to list to download...");
                    toDownload.add(newest);
                    return;
                }

                if (!newest.matches(foundOnPC)) {
                    System.out.println("Library '" + foundOnPC.getFileName() + "' found on PC, but is outdated, adding to list to download...");
                    toRemove.add(foundOnPC);
                    toDownload.add(newest);
                }

                System.out.println("Library '" + foundOnPC.getFileName() + "' found on PC and is up to date.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return !toRemove.isEmpty() || !toDownload.isEmpty();
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    //HELPER METHODS
    private JsonArray parseLibrariesJson(Reader reader) throws IOException {
        Gson gson = new GsonBuilder().create();
        JsonElement libraries = gson.fromJson(reader, JsonElement.class);
        String version = libraries.getAsJsonObject().get("version").getAsString();
        log("Library Version: " + version);
        return libraries.getAsJsonObject().get("libraries").getAsJsonArray();
    }
    private Library decode(JsonObject json) {
        String fullName = json.get("name").getAsString();
        String repo = json.get("repo").getAsString();
        String md5 = json.get("md5").getAsString();
        String ext = "jar";

        boolean md5_skip = false;
        if (json.get("md5_skip") != null)
            md5_skip = json.get("md5_skip").getAsBoolean();

        if (json.get("ext") != null)
            ext = json.get("ext").getAsString().replace(".", "");

        //split the full artifact name by ':'
        String[] name = fullName.split(":");

        String group = name[0].replace(".", "/");
        String artifact = name[1];
        String version = name[2];
        String classifier = "";

        if (name.length == 4) {
            classifier = version.contains("-SNAPSHOT") ? "-" + name[3] : name[3];
            version = version.replace("-SNAPSHOT", "");
        }

        return new Library(repo, group, artifact, version, classifier, ext, md5, md5_skip);
    }
    private Library getLibraryFromPC(Library library) throws Exception {
        //Get the path from library.getPath() and walk through the folder and add any jar that matches the name without version to the list
        File path = new File(LIBRARIES_FOLDER, library.getPath());
        if (!path.exists())
            return null;

        File[] files = Arrays.stream(path.listFiles()).filter(file -> file.getName().contains(library.artifact)).toArray(File[]::new);

        if (files.length == 0)
            return null;

        for (File file : files) {
            String name = file.getName();
            if (name.contains(library.artifact + "-" + library.version)) {
                return new Library(library.repo, library.group, library.artifact, library.version, library.classifier, library.ext, MD5.get(path + "/" + name), library.skipMD5);
            }
        }

        return null;
        /*File first = files[0];
        String fileName = first.getName();

        System.out.println("First file: " + fileName); //TODO: DELETE

        String repo = library.repo;
        String group = library.group;
        String artifact = library.artifact;
        String classifier = library.classifier;

        int index = fileName.indexOf(artifact) + artifact.length() + 1;
        String version = fileName.substring(index, fileName.indexOf("." + library.ext));
        if (!classifier.isEmpty())
            version = fileName.substring(index, fileName.indexOf(classifier));

        String ext = library.ext;

        return new Library(repo, group, artifact, version, classifier, ext, MD5.get(path + "/" + fileName), library.skipMD5);*/
    }
    private File downloadLibrary(Library lib, File file, int tries) throws IOException, LibraryDownloadException {
        if (tries == 0)
            log(String.format("%s%n", "Downloading Jar: " + file.getName()));

        file.getParentFile().mkdirs();
        file.createNewFile();

        URL url = URI.create(lib.getURL()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", LibraryDownloader.class.getSimpleName());

        try (ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream())) {
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        }

        if (!lib.checkMD5(file)) {
            if (tries < 3) {
                logWriter.write("The downloaded file '" + file.getName() + "' has an invalid MD5 checksum. Redownloading... Try (" + ++tries + "/3)");
                file.delete();
                downloadLibrary(lib, file, tries);
            } else {
                System.out.println();
                error("Failed to download file '" + file.getName() + "' after 3 tries. Aborting...");
                logWriter.close();
                throw new LibraryDownloadException(lib.getFileName());
            }
        }
        return file;
    }
    private void log(String message) throws IOException {
        System.out.println(message);
        write("INFO - " + message);
    }
    private void error(String message) throws IOException {
        System.err.println(message);
        write("ERROR - " + message);
    }
    private void write(String message) throws IOException {
        if (logWriter != null)
            logWriter.write(message);
    }
}
