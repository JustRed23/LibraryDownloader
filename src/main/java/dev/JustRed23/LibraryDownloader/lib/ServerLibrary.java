package dev.JustRed23.LibraryDownloader.lib;

public final class ServerLibrary extends Library {

    private final String downloadURL;
    private final String jarName;
    private final String md5;

    public ServerLibrary(String downloadURL, String jarName, String md5) {
        super("", "", "", "", "", "jar", md5, false);
        this.downloadURL = downloadURL;
        this.jarName = jarName;
        this.md5 = md5;
    }

    public String getURL() {
        return downloadURL;
    }

    public String getFileName() {
        return jarName + "." + ext;
    }
}
