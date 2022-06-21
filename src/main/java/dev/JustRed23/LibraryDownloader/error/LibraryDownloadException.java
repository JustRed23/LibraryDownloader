package dev.JustRed23.LibraryDownloader.error;

public class LibraryDownloadException extends Exception {

    public LibraryDownloadException(String libraryName) {
        super("Failed to download library '" + libraryName + "' after 3 tries.");
    }
}
