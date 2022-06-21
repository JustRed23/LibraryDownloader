package dev.JustRed23.LibraryDownloader;

import dev.JustRed23.LibraryDownloader.utils.MD5;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class Library {

    public final String repo;
    public final String group;
    public final String artifact;
    public final String version;
    public final String classifier;
    public final String ext;

    private final String md5;
    public final boolean skipMD5;

    /**
     * Creates a new Library object.
     * @param repo The repository of the library.
     *  Example: <a href="https://repo.maven.apache.org/maven2">https://repo.maven.apache.org/maven2</a>
     * @param group The group of the library.
     *  Example: 'dev.JustRed23'
     * @param artifact The artifact of the library.
     *  Example: 'LibraryDownloader'
     * @param version The version of the library.
     *  Example: '1.0.0'
     * @param classifier The classifier of the library.
     *  Example: '-fatjar'
     * @param ext The extension of the library.
     *  Example: 'jar'
     * @param md5 The checksum of the library.
     *  Example: 'b8f8f8f8f8f8f8f8f8f8f8f8f8f8f8f'
     * @param skipMD5 Whether to skip the MD5 check.
     */
    public Library(String repo, String group, String artifact, String version, String classifier, String ext, String md5, boolean skipMD5) {
        this.repo = repo.endsWith("/") ? repo.substring(0, repo.length() - 1) : repo;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
        this.ext = ext;
        this.md5 = md5;
        this.skipMD5 = skipMD5;
    }

    public boolean checkMD5(File lib) throws IOException {
        return skipMD5 || MD5.checkMD5(lib, this.md5);
    }

    public String getURL() {
        return repo + "/" + group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + classifier + "." + ext;
    }

    public String getPath() {
        return group.replace(".", "/") + "/" + artifact + "/";
    }

    public String getFileName() {
        return artifact + "-" + version + classifier + "." + ext;
    }

    public boolean matches(Library other) {
        return other.getURL().equals(getURL());
    }

    public String toString() {
        return "Library{" +
                "repo='" + repo + '\'' +
                ", group='" + group + '\'' +
                ", artifact='" + artifact + '\'' +
                ", version='" + version + '\'' +
                ", classifier='" + classifier + '\'' +
                ", ext='" + ext + '\'' +
                ", md5='" + md5 + '\'' +
                ", skipMD5=" + skipMD5 +
                '}';
    }
}
