package io.github.alexbujenita.amberfic.core;

import java.util.List;

record Results(List<ScannedFile> scannedFiles, List<IgnoredEntry> ignored) {
    Results {
        scannedFiles = List.copyOf(scannedFiles);
        ignored = List.copyOf(ignored);
    }
}
