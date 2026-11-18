package io.github.alexbujenita.amberfic.core;

import io.github.alexbujenita.amberfic.core.exceptions.ManifestAlreadyExistsException;
import io.github.alexbujenita.amberfic.core.exceptions.SourceDirectoryNotFoundException;
import io.github.alexbujenita.amberfic.core.exceptions.SourceNotDirectoryException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class Amberfic {

    private Amberfic() {}

    public static void create(Path root) throws IOException {
        final Hasher hasher = new Hasher();
        final String manifestFileName = ManifestFiles.fileName(hasher.getAlgorithm());

        // check the source exists and that it is a directory
        try {
            final BasicFileAttributes attrs = Files.readAttributes(root, BasicFileAttributes.class);
            if (!attrs.isDirectory()) {
                throw new SourceNotDirectoryException(root.toString());
            }
        } catch (NoSuchFileException nsfe) {
            throw new SourceDirectoryNotFoundException(root.toString(), nsfe, "Input source not found.");
        }

        // check the manifest, there shouldn't be one
        final Path manifestFilePath = root.resolve(manifestFileName);

        if (Files.exists(manifestFilePath)) {
            throw new ManifestAlreadyExistsException(manifestFilePath.toString());
        }

        final Results walkResults = new FileWalker().walk(root);
        final List<ChecksumEntry> entries =
                new ArrayList<>(walkResults.scannedFiles().size());

        for (final ScannedFile scannedFile : walkResults.scannedFiles()) {
            try (InputStream inputStream = Files.newInputStream(scannedFile.path())) {
                entries.add(new ChecksumEntry(hasher.hash(inputStream), scannedFile.relativeName()));
            }
        }

        final ChecksumWriter checksumWriter = new ChecksumWriter(entries);
        checksumWriter.write(manifestFilePath);
    }
}
