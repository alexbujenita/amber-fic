package io.github.alexbujenita.amberfic.core;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class JunkFilter {
    private JunkFilter() {
        /* This utility class should not be instantiated */
    }

    private static final Set<String> JUNK_DIRS = Set.of(
            // macos
            ".fseventsd",
            // linux
            ".directory");
    private static final Set<String> JUNK_FILES = Set.of(
            // windows
            "Thumbs.db",
            "ehthumbs.db",
            "ehthumbs_vista.db",
            "desktop.ini",
            // macos
            ".DS_Store");
    private static final Set<String> PROGRAM_FILES = Set.of("amberfic_checksum.sha512");
    private static final Set<String> JUNK = Stream.of(JUNK_DIRS, JUNK_FILES, PROGRAM_FILES)
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    private static final String APPLE_DOUBLE_PREFIX = "._";

    // TODO rename!?
    static boolean isJunk(Path file) {
        final String fileName = file.getFileName().toString();
        return JUNK.contains(fileName) || (fileName.startsWith(APPLE_DOUBLE_PREFIX));
    }

    static boolean isJunkFolder(Path file) {
        final String fileName = file.getFileName().toString();
        return JUNK_DIRS.contains(fileName);
    }
}
