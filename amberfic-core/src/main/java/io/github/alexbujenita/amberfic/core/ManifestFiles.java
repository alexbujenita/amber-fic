package io.github.alexbujenita.amberfic.core;

final class ManifestFiles {

    static final String BASENAME = "amberfic_checksum";
    static final String TMP_SUFFIX = ".tmp";

    private ManifestFiles() {}

    static String fileName(HashAlgorithm algorithm) {
        return BASENAME + "." + algorithm.extension();
    }
}
