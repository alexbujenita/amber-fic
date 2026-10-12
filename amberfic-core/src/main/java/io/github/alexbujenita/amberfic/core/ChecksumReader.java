package io.github.alexbujenita.amberfic.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

class ChecksumReader {

    List<ChecksumEntry> parseChecksum(Reader reader) throws IOException {
        final List<ChecksumEntry> checksumEntries = new ArrayList<>();
        String line;
        BufferedReader br = new BufferedReader(reader);
        while ((line = br.readLine()) != null) {
            addChecksumToList(line, checksumEntries);
        }

        return List.copyOf(checksumEntries);
    }

    private void addChecksumToList(String line, List<ChecksumEntry> checksumEntries) {
        String[] parts = line.split(" {2}");
        checksumEntries.add(new ChecksumEntry(parts[0], parts[1]));
    }
}
