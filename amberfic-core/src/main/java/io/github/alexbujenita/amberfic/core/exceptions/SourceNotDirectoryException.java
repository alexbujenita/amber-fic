package io.github.alexbujenita.amberfic.core.exceptions;

import java.nio.file.NotDirectoryException;

/**
 * Thrown when the source passed is not a directory.
 */
@SuppressWarnings("java:S110")
public class SourceNotDirectoryException extends NotDirectoryException {
    public SourceNotDirectoryException(String file) {
        super(file);
    }
}
