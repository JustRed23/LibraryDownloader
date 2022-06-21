package dev.JustRed23.LibraryDownloader.utils;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5 {

    public static boolean checkMD5(@NotNull File file, @NotNull String md5sum) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LogManager.getLogger(MD5.class).error("MD5 algorithm not found");
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[1024];
            int read;
            while ((read = fis.read(data)) != -1) {
                digest.update(data, 0, read);
            }
        }

        byte[] md5sumBytes = fromHexString(md5sum);
        byte[] bytes = digest.digest();

        for (int i = 0; i < md5sumBytes.length; i++) {
            if (md5sumBytes[i] != bytes[i]) {
                return false;
            }
        }

        return true;
    }

    private static byte[] fromHexString(@NotNull String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
