package io.github.alexbujenita.amberfic.core;

import java.nio.file.FileAlreadyExistsException;

/**
 * Thrown when the temporary file used to write a manifest already exists.
 *
 * <p>An earlier write that crashed or was killed can leave it behind, or another
 * run may be writing the same manifest right now. If no other run is active, the
 * file is stale and can be deleted.
 */
@SuppressWarnings("java:S110")
public class StaleTempFileException extends FileAlreadyExistsException {
    public StaleTempFileException(String file, FileAlreadyExistsException cause) {
        super(file);
        initCause(cause);
    }
}
