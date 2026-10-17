package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.HashAlgorithm.SHA512;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.thenCode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    void shouldPreserveUnicodeChars() throws IOException {
        // given an entry
        final String entries = "hash_123  caf\u00E9.png";
        StringReader reader = new StringReader(entries);

        // when we call the checksumReader
        final ChecksumReader checksumReader = new ChecksumReader();
        final List<ChecksumEntry> checksumEntries = checksumReader.parseChecksum(reader);

        // then we have the correct list returned
        assertThat(checksumEntries).extracting(ChecksumEntry::filePath).containsExactly("caf\u00E9.png");
        assertThat(checksumEntries).extracting(ChecksumEntry::hash).containsExactly("hash_123");
    }

    @Test
    void correctlyReadsChecksumGeneratedByOtherTools() throws IOException, URISyntaxException {
        // given a checksum file generated with `shasum -a 512 external_sha512.txt > amberfic_checksum.sha512`
        ClassLoader classLoader = getClass().getClassLoader();
        Path file = Path.of(classLoader.getResource("amberfic_checksum.sha512").toURI());

        // when we call the checksumReader
        final ChecksumReader checksumReader = new ChecksumReader();
        final ChecksumManifest manifest = checksumReader.parseChecksum(file);
        final List<ChecksumEntry> checksumEntries = manifest.entries();

        // then we have the correct list returned
        assertThat(checksumEntries).extracting(ChecksumEntry::filePath).containsExactly("external_sha512.txt");
        assertThat(checksumEntries)
                .extracting(ChecksumEntry::hash)
                .containsExactly(
                        "30828df258f0f01ca423a681121a9d4405082158bb998c0b95780c2a64dc9461f78c730df0e261e36b801a4728fa808391746ce16c6530162f9a67e2ff48afd0");
    }

    @Test
    void preservesSpacesInFileNames() throws IOException {
        // given an entry
        final String entry = "hash_123  Holiday  Bangladesh/photo.png";
        final StringReader reader = new StringReader(entry);

        // when we call the checksumReader
        final ChecksumReader checksumReader = new ChecksumReader();
        final List<ChecksumEntry> checksumEntries = checksumReader.parseChecksum(reader);

        // then we have the correct list returned
        assertThat(checksumEntries)
                .extracting(ChecksumEntry::filePath)
                .containsExactly("Holiday  Bangladesh/photo.png");
        assertThat(checksumEntries).extracting(ChecksumEntry::hash).containsExactly("hash_123");
    }

    @Test
    void returnsAListOfChecksumsFromAFileAndTheCorrectAlgorithm(@TempDir Path path) throws IOException {
        // given a file with some checksum results
        final Path checksumFile = Files.createFile(path.resolve("checksum.sha512"));
        final String entries = """
                hash_123  sub/b.png
                hash_456  a.png
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entries);
        }

        // when we call the checksumReader
        final ChecksumReader checksumReader = new ChecksumReader();
        final ChecksumManifest manifest = checksumReader.parseChecksum(checksumFile);
        final List<ChecksumEntry> checksumEntries = manifest.entries();
        final HashAlgorithm algorithm = manifest.algorithm();

        // then we have the correct list returned and the correct algorithm used
        assertThat(checksumEntries).extracting(ChecksumEntry::filePath).containsExactly("sub/b.png", "a.png");
        assertThat(checksumEntries).extracting(ChecksumEntry::hash).containsExactly("hash_123", "hash_456");
        assertThat(algorithm).isEqualTo(SHA512);
    }

    @ParameterizedTest(name = "Has correct algorithm: {1}")
    @CsvSource({"checksum.sha512,    SHA512", "checksum.sha256,    SHA256"})
    void hasTheCorrectAlgorithm(String fileName, String expectedAlgorithm, @TempDir Path path) throws IOException {
        // given a file with some checksum results
        final Path checksumFile = Files.createFile(path.resolve(fileName));
        final String entries = """
                hash_123  sub/b.png
                hash_456  a.png
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entries);
        }

        // when we call the checksumReader
        final ChecksumReader checksumReader = new ChecksumReader();
        final ChecksumManifest manifest = checksumReader.parseChecksum(checksumFile);
        final HashAlgorithm algorithm = manifest.algorithm();

        // then we have the correct algorithm detected
        assertThat(algorithm).isEqualTo(HashAlgorithm.valueOf(expectedAlgorithm));
    }

    @Test
    void throwsWhenNoAlgorithmIsFound(@TempDir Path path) throws IOException {
        // given a file with a non-existing algo with some checksum results
        final Path checksumFile = Files.createFile(path.resolve("checksum.sha883"));
        final String entries = """
                hash_123  sub/b.png
                hash_456  a.png
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entries);
        }

        // when we call the checksumReader then an error is thrown
        final ChecksumReader checksumReader = new ChecksumReader();
        assertThatThrownBy(() -> checksumReader.parseChecksum(checksumFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("sha883");
    }

    @Test
    void throwsWhenNoFileExtensionIsFound(@TempDir Path path) throws IOException {
        // given a file without an extension
        final Path checksumFile = Files.createFile(path.resolve("checksum_maybe"));
        final String entries = """
                hash_123  sub/b.png
                hash_456  a.png
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entries);
        }

        // when we call the checksumReader then an error is thrown
        final ChecksumReader checksumReader = new ChecksumReader();
        assertThatThrownBy(() -> checksumReader.parseChecksum(checksumFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("checksum_maybe");
    }

    @Test
    void throwsWithMalformedChecksum(@TempDir Path path) throws IOException {
        // given a file with some broken checksum results
        final Path checksumFile = Files.createFile(path.resolve("checksum.sha512"));
        final String entries = """
                hash_123  sub/b.png
                broken
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entries);
        }

        // when we call the checksumReader then an exception is thrown
        final ChecksumReader checksumReader = new ChecksumReader();
        assertThatThrownBy(() -> checksumReader.parseChecksum(checksumFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("line 2");
    }

    @Test
    void rejectsEntriesWithEmptyFileName(@TempDir Path path) throws IOException {
        // given a file with some broken checksum results
        final Path checksumFile = Files.createFile(path.resolve("checksum.sha512"));
        final String entries = "hash_123  a.png\nhash_456  \n";

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entries);
        }

        // Expecting code to raise a throwable.
        final ChecksumReader checksumReader = new ChecksumReader();
        assertThatThrownBy(() -> checksumReader.parseChecksum(checksumFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("line 2");
    }

    @Test
    void returnsAListOfChecksumsDifferentEveryTime() throws IOException {
        // given a string of entries
        final String entry1 = "hash_123  sub/b.png";
        final String entry2 = "hash_456  a.png";

        final StringReader reader1 = new StringReader(entry1);
        final StringReader reader2 = new StringReader(entry2);

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

    @Test
    void throwsIfTheFileDoesntExist(@TempDir Path path) {

        // given / when we call the checksumReader with a missing file
        final ChecksumReader checksumReader = new ChecksumReader();

        // then we have the correct exception
        assertThatThrownBy(() -> checksumReader.parseChecksum(path.resolve("checksum.sha512")))
                .isInstanceOf(NoSuchFileException.class);
    }
}
