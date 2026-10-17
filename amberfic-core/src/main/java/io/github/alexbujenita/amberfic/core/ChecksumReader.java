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

    ChecksumManifest parseChecksum(Path path) throws IOException {
        final HashAlgorithm algorithm = extractAlgorithm(path);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            final List<ChecksumEntry> checksumEntries = parseChecksum(reader);
            return new ChecksumManifest(checksumEntries, algorithm);
        }
    }

    private HashAlgorithm extractAlgorithm(Path path) throws IOException {
        final String fileExtension = getFileExtension(path.getFileName().toString());
        try {
            return HashAlgorithm.valueOf(fileExtension.toUpperCase());
        } catch (IllegalArgumentException _) {
            throw new IOException("Invalid hash algorithm: " + fileExtension);
        }
    }

    private String getFileExtension(String filename) throws IOException {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        throw new IOException("Missing file extension: " + filename);
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
