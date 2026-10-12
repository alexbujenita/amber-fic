package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.thenCode;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChecksumReaderTest {

    @Test
    void returnsAListOfChecksums() throws IOException {
        // given a string of entries
        final String entries = """
                hash_123  sub/b.png
                hash_456  a.png
                """;
        StringReader reader = new StringReader(entries);

        // when we call the checksumReader
        final ChecksumReader checksumReader = new ChecksumReader();
        final List<ChecksumEntry> checksumEntries = checksumReader.parseChecksum(reader);

        // then we have the correct list returned
        assertThat(checksumEntries).extracting(ChecksumEntry::filePath).containsExactly("sub/b.png", "a.png");
        assertThat(checksumEntries).extracting(ChecksumEntry::hash).containsExactly("hash_123", "hash_456");

        // and the reader is not closed
        thenCode(reader::read).doesNotThrowAnyException();
    }

    @Test
    void returnsAListOfChecksumsDifferentEveryTime() throws IOException {
        // given a string of entries
        final String entry1 = "hash_123  sub/b.png";
        final String entry2 = "hash_456  a.png";

        StringReader reader1 = new StringReader(entry1);
        StringReader reader2 = new StringReader(entry2);

        // when we call the same checksumReader with different readers
        final ChecksumReader checksumReader = new ChecksumReader();
        final List<ChecksumEntry> checksumEntries1 = checksumReader.parseChecksum(reader1);
        final List<ChecksumEntry> checksumEntries2 = checksumReader.parseChecksum(reader2);

        // then we have the correct list returned
        assertThat(checksumEntries1).extracting(ChecksumEntry::filePath).containsExactly("sub/b.png");
        assertThat(checksumEntries2).extracting(ChecksumEntry::filePath).containsExactly("a.png");
        assertThat(checksumEntries1).extracting(ChecksumEntry::hash).containsExactly("hash_123");
        assertThat(checksumEntries2).extracting(ChecksumEntry::hash).containsExactly("hash_456");
    }
}
