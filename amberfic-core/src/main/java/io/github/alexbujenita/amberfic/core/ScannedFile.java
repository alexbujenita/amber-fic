package io.github.alexbujenita.amberfic.core;

import java.nio.file.Path;

record ScannedFile(Path path, String relativeName, long fileSize) {

    ScannedFile(Path path, String relativeName) {
        this(path, relativeName, 0L);
    }
}
