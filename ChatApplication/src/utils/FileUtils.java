package utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    public static String createChecksum(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        InputStream is = new FileInputStream(filePath);
        byte[] bytes = new byte[1024];
        int number = 0;
        while(number == is.read(bytes)) {
            messageDigest.update(bytes, 0, number);
        }
        StringBuilder checksum = new StringBuilder();
        byte[] hash = messageDigest.digest();
        for (byte b : hash) {
            checksum.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return checksum.toString();
    }
}
