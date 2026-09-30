package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class Hasher {

    private static final int BUFFER_SIZE = 1024;
    private static final HexFormat HEX = HexFormat.of();

    private final HashAlgorithm algorithm;

    public Hasher() {
        this(HashAlgorithm.SHA512);
    }

    public Hasher(HashAlgorithm algorithm) {
        Objects.requireNonNull(algorithm, "Algorithm cannot be null.");
        this.algorithm = algorithm;
    }

    public String hash(InputStream input) throws IOException {
        final MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance(algorithm.jdkName());
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("Algorithm " + algorithm.jdkName() + " not found.", nsae);
        }

        final byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, bytesRead);
        }

        return HEX.formatHex(messageDigest.digest());
    }
}
