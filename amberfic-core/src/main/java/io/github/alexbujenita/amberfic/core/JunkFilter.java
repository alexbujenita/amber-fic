package io.github.alexbujenita.amberfic.core;

import java.nio.file.Path;
import java.util.Set;

final class JunkFilter {
    private JunkFilter() {
        /* This utility class should not be instantiated */
    }

    private static final Set<String> JUNK_FILES = Set.of(
            // windows
            "Thumbs.db",
            "ehthumbs.db",
            "ehthumbs_vista.db",
            "desktop.ini",
            // macos
            ".DS_Store",
            ".fseventsd",
            // linux
            ".directory");
    private static final String APPLE_DOUBLE_PREFIX = "._";

    static boolean isJunk(Path file) {
        final String fileName = file.getFileName().toString();
        return JUNK_FILES.contains(fileName) || (fileName.startsWith(APPLE_DOUBLE_PREFIX));
    }
}
