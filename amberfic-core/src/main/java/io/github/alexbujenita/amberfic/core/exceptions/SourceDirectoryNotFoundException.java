package io.github.alexbujenita.amberfic.core.exceptions;

import java.nio.file.NoSuchFileException;

/**
 * Thrown when the source directory doesn't exist.
 */
@SuppressWarnings("java:S110")
public class SourceDirectoryNotFoundException extends NoSuchFileException {
    public SourceDirectoryNotFoundException(String file, NoSuchFileException cause, String reason) {
        super(file, null, reason);
        initCause(cause);
    }
}
