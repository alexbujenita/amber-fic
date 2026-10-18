package io.github.alexbujenita.amberfic.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ChecksumWriter {
    private final List<ChecksumEntry> entries;

    private static final Comparator<ChecksumEntry> COMPARE_PATHS =
            Comparator.comparing(ChecksumEntry::filePath, String::compareTo);

    ChecksumWriter(List<ChecksumEntry> entries) {
        this.entries = new ArrayList<>(entries);
        this.entries.sort(COMPARE_PATHS);
    }

    void write(Writer writer) throws IOException {

        for (ChecksumEntry hashedResult : entries) {
            writer.write(hashedResult.entry());
            writer.write("\n");
        }
    }

    void write(Path hashResultPath) throws IOException {
        // create a tmp file to write on in case of any errors
        Path tmpFile = Files.createFile(hashResultPath.resolveSibling(hashResultPath.getFileName() + ".tmp"));

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
                this.write(writer);
            }

            // if we're here, all good, we can replace the old file
            Files.move(tmpFile, hashResultPath, StandardCopyOption.ATOMIC_MOVE);
            // and cleanup the tmp file in case of any previous error
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }
}
