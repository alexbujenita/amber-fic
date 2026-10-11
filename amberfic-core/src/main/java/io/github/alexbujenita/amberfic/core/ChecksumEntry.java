package io.github.alexbujenita.amberfic.core;

record ChecksumEntry(String hash, String filePath) {
    private static final String DELIMITER = "  ";

    String entry() {
        return "%s%s%s".formatted(hash, DELIMITER, filePath);
    }
}
