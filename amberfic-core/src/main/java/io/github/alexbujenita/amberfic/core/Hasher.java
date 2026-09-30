package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class Hasher {

    private static final String SHA_512 = "SHA-512";
    private static final int BUFFER_SIZE = 1024;

    private static final HexFormat HEX = HexFormat.of();

    public String hash(InputStream input) throws IOException {
        final MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance(SHA_512);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not found: " + SHA_512, e);
        }

        final byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, bytesRead);
        }

        return HEX.formatHex(messageDigest.digest());
    }
}
