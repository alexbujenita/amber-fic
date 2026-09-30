package io.github.alexbujenita.amberfic.core;

public enum HashAlgorithm {
    SHA256("SHA-256"),
    SHA512("SHA-512");

    private final String jdkName;

    HashAlgorithm(String jdkName) {
        this.jdkName = jdkName;
    }

    public String jdkName() {
        return jdkName;
    }
}
