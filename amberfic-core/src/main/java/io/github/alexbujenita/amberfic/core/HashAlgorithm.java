package io.github.alexbujenita.amberfic.core;

enum HashAlgorithm {
    SHA256("SHA-256", "sha256"),
    SHA512("SHA-512", "sha512");

    private final String jdkName;
    private final String extension;

    HashAlgorithm(String jdkName, String extension) {
        this.jdkName = jdkName;
        this.extension = extension;
    }

    static HashAlgorithm fromExtension(String extension) {
        for (HashAlgorithm algorithm : values()) {
            if (algorithm.extension.equalsIgnoreCase(extension)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid algorithm provided: %s".formatted(extension));
    }

    String jdkName() {
        return jdkName;
    }

    String extension() {
        return extension;
    }
}
