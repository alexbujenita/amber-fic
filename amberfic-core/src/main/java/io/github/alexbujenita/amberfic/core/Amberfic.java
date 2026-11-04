package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Amberfic {

    private Amberfic() {}

    static void create(Path root) throws IOException {
        final Results walkResults = new FileWalker().walk(root);
        final Hasher hasher = new Hasher();
        final List<ChecksumEntry> entries =
                new ArrayList<>(walkResults.scannedFiles().size());

        for (final ScannedFile scannedFile : walkResults.scannedFiles()) {
            try (final InputStream is = Files.newInputStream(scannedFile.path())) {
                entries.add(new ChecksumEntry(hasher.hash(is), scannedFile.relativeName()));
            }
        }

        final ChecksumWriter checksumWriter = new ChecksumWriter(entries);
        checksumWriter.write(root.resolve(ManifestFiles.fileName(hasher.getAlgorithm())));
    }
}
