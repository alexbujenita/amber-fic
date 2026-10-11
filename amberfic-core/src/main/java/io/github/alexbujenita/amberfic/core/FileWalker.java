package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class FileWalker {

    private static final Comparator<ScannedFile> COMPARE_FILE_NAMES =
            Comparator.comparing(ScannedFile::relativeName, String::compareTo);

    /**
     * Traverses the root path provided.
     * @param path The root path where we start traversing
     * @return a results object containing files to hash and an ignored list if needed for display
     * @throws IOException Mostly OS and FS errors
     */
    Results walk(Path path) throws IOException {
        return this.walk(path, false);
    }

    Results walk(Path path, boolean includeHidden) throws IOException {
        List<ScannedFile> scannedFiles = new ArrayList<>();
        List<IgnoredEntry> ignoredEntries = new ArrayList<>();
        Files.walkFileTree(path, new FileSelectorVisitor(scannedFiles, ignoredEntries, path, includeHidden));
        scannedFiles.sort(COMPARE_FILE_NAMES);

        return new Results(scannedFiles, ignoredEntries);
    }
}
