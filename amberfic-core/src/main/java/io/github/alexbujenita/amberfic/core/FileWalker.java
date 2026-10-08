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

    List<ScannedFile> walk(Path path) throws IOException {
        List<ScannedFile> scannedFiles = new ArrayList<>();
        Files.walkFileTree(path, new IgnoreJunkVisitor(scannedFiles, path));
        scannedFiles.sort(COMPARE_FILE_NAMES);
        return scannedFiles;
    }
}
