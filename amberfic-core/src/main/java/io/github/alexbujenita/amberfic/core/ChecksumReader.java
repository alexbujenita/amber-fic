package io.github.alexbujenita.amberfic.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class ChecksumReader {

    private static final String TWO_SPACES = " {2}";

    List<ChecksumEntry> parseChecksum(Reader reader) throws IOException {
        int currentLineNumber = 0;
        final List<ChecksumEntry> checksumEntries = new ArrayList<>();
        String line;
        BufferedReader br = new BufferedReader(reader);
        while ((line = br.readLine()) != null) {
            addChecksumToList(line, checksumEntries, ++currentLineNumber);
        }

        return List.copyOf(checksumEntries);
    }

    List<ChecksumEntry> parseChecksum(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parseChecksum(reader);
        }
    }

    private void addChecksumToList(String line, List<ChecksumEntry> checksumEntries, int currentLineNumber)
            throws IOException {
        String[] parts = line.split(TWO_SPACES, 2);
        if (parts.length != 2 || parts[1].isEmpty()) {
            throw new IOException("Error reading checksum file, check line %s.".formatted(currentLineNumber));
        }
        checksumEntries.add(new ChecksumEntry(parts[0], parts[1]));
    }
}
