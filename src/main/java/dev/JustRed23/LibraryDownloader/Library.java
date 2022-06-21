package dev.JustRed23.LibraryDownloader;

public class Library {

    private final String repo;
    private final String group;
    private final String artifact;
    private final String version;

    private final String MD5;
    private final boolean skipMD5;

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
     * @param MD5 The MD5 of the library.
     *  Example: 'b8f8f8f8f8f8f8f8f8f8f8f8f8f8f8f'
     * @param skipMD5 Whether to skip the MD5 check.
     */
    public Library(String repo, String group, String artifact, String version, String MD5, boolean skipMD5) {
        this.repo = repo;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.MD5 = MD5;
        this.skipMD5 = skipMD5;
    }
}
