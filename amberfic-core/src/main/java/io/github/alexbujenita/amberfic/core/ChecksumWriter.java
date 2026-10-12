package io.github.alexbujenita.amberfic.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        try (BufferedWriter writer = Files.newBufferedWriter(hashResultPath, StandardCharsets.UTF_8)) {
            this.write(writer);
        }
    }
}
