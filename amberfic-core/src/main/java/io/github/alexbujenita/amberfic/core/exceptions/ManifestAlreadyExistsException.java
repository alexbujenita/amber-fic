package io.github.alexbujenita.amberfic.core.exceptions;

import java.nio.file.FileAlreadyExistsException;

/**
 * Thrown when the manifest file already exists.
 *
 * <p>A {@code create} command shouldn't overwrite an existing manifest.
 * Could be invoked by mistake, the risk is to overwrite good values
 * with new corrupted hashes.
 */
@SuppressWarnings("java:S110")
public class ManifestAlreadyExistsException extends FileAlreadyExistsException {
    private static final String MANIFEST_EXISTS_ERROR = "The manifest already exists.";

    public ManifestAlreadyExistsException(String file) {
        super(file, null, MANIFEST_EXISTS_ERROR);
    }
}
