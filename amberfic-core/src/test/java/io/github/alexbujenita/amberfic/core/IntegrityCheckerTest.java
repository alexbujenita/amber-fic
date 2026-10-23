package io.github.alexbujenita.amberfic.core;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class IntegrityCheckerTest {

    static final String ABC_SHA_512 =
            "397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119";
    static final String ABC = "ABC";

    static Stream<Arguments> testResultsProvider() {
        return Stream.of(
                argumentSet(
                        "new beside known",
                        List.of("photo.jpg", "extra.jpg"),
                        new ChecksumManifest(
                                List.of(new ChecksumEntry(ABC_SHA_512, "photo.jpg")), HashAlgorithm.SHA512),
                        List.of(
                                new IntegrityCheckResult.Passed("photo.jpg"),
                                new IntegrityCheckResult.MissingFromManifest("extra.jpg"))),
                argumentSet(
                        "missing beside known",
                        List.of("photo.jpg"),
                        new ChecksumManifest(
                                List.of(
                                        new ChecksumEntry(ABC_SHA_512, "photo.jpg"),
                                        new ChecksumEntry(ABC_SHA_512, "gone.jpg")),
                                HashAlgorithm.SHA512),
                        List.of(
                                new IntegrityCheckResult.Passed("photo.jpg"),
                                new IntegrityCheckResult.MissingFromScan("gone.jpg"))),
                argumentSet(
                        "empty scan",
                        emptyList(),
                        new ChecksumManifest(
                                List.of(new ChecksumEntry(ABC_SHA_512, "photo.jpg")), HashAlgorithm.SHA512),
                        List.of(new IntegrityCheckResult.MissingFromScan("photo.jpg"))),
                // mvp is to match by path
                argumentSet(
                        "rename",
                        List.of("renamed.jpg"),
                        new ChecksumManifest(List.of(new ChecksumEntry(ABC_SHA_512, "old.jpg")), HashAlgorithm.SHA512),
                        List.of(
                                new IntegrityCheckResult.MissingFromScan("old.jpg"),
                                new IntegrityCheckResult.MissingFromManifest("renamed.jpg"))),
                argumentSet(
                        "move to sub folder",
                        List.of("sub/photo.jpg"),
                        new ChecksumManifest(
                                List.of(new ChecksumEntry(ABC_SHA_512, "photo.jpg")), HashAlgorithm.SHA512),
                        List.of(
                                new IntegrityCheckResult.MissingFromScan("photo.jpg"),
                                new IntegrityCheckResult.MissingFromManifest("sub/photo.jpg"))));
    }

    @Test
    void checksIntegrityOfAPreHashedFile(@TempDir Path path) throws Exception {
        // Given a file with some content and a checksum manifest
        final String fileName = "photo.jpg";
        final Path photo = Files.createFile(path.resolve(fileName));

        try (BufferedWriter writer = Files.newBufferedWriter(photo)) {
            writer.write(ABC);
        }

        final ScannedFile scannedFile = new ScannedFile(photo, fileName);

        final ChecksumManifest manifest =
                new ChecksumManifest(List.of(new ChecksumEntry(ABC_SHA_512, fileName)), HashAlgorithm.SHA512);

        // when we call the integrity checker
        final IntegrityChecker checker = new IntegrityChecker(List.of(scannedFile), manifest);
        final List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we have a successful result
        assertThat(results).hasSize(1).contains(new IntegrityCheckResult.Passed(fileName));
    }

    @Test
    void listIsNotReusedInCaseOfErrors(@TempDir Path path) throws Exception {
        // Given a file with some content and a checksum manifest and a non existing one
        final String fileName = "photo.jpg";
        final String nonExistent = "not_yet.jpg";
        final Path nonExistentFile = path.resolve(nonExistent);
        final Path photo = Files.createFile(path.resolve(fileName));
        final String emptyHash =
                "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

        try (BufferedWriter writer = Files.newBufferedWriter(photo)) {
            writer.write(ABC);
        }

        final ScannedFile scannedFile = new ScannedFile(photo, fileName);
        final ScannedFile nonExistingScannedFile = new ScannedFile(nonExistentFile, nonExistent);

        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(new ChecksumEntry(ABC_SHA_512, fileName), new ChecksumEntry(emptyHash, nonExistent)),
                HashAlgorithm.SHA512);

        // when we call the integrity checker and it throws
        final IntegrityChecker checker = new IntegrityChecker(List.of(scannedFile, nonExistingScannedFile), manifest);
        assertThatThrownBy(checker::calculateIntegrityResults).isInstanceOf(NoSuchFileException.class);

        // and the file is there and we call the same integrity instance
        Files.createFile(nonExistentFile);
        final List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we only have the expected results
        assertThat(results)
                .hasSize(2)
                .contains(new IntegrityCheckResult.Passed(fileName), new IntegrityCheckResult.Passed(nonExistent));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADC", ""})
    void checksIntegrityOfAPreHashedFile_hashMismatchShouldFail(String fileContents, @TempDir Path path)
            throws Exception {
        // Given a file with some content and a checksum manifest that doesn't match the content
        final String fileName = "photo.jpg";
        final Path photo = Files.createFile(path.resolve(fileName));
        // the precomputed hash is for ABC

        try (BufferedWriter writer = Files.newBufferedWriter(photo)) {
            writer.write(fileContents);
        }

        final ScannedFile scannedFile = new ScannedFile(photo, fileName);

        final ChecksumManifest manifest =
                new ChecksumManifest(List.of(new ChecksumEntry(ABC_SHA_512, fileName)), HashAlgorithm.SHA512);

        // when we call the integrity checker
        final IntegrityChecker checker = new IntegrityChecker(List.of(scannedFile), manifest);
        final List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we have a failed result
        assertThat(results).hasSize(1).contains(new IntegrityCheckResult.Failed(fileName));
    }

    @ParameterizedTest
    @MethodSource("testResultsProvider")
    void behavesAsExpected(
            List<String> scannedNames,
            ChecksumManifest manifest,
            List<IntegrityCheckResult> expectedResults,
            @TempDir Path path)
            throws Exception {

        // given some files
        final List<ScannedFile> scannedFileList = new ArrayList<>();
        for (String fileName : scannedNames) {
            final Path file = path.resolve(fileName);
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            scannedFileList.add(new ScannedFile(file, fileName));
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write(ABC);
            }
        }

        // when we call the integrity checker
        final IntegrityChecker checker = new IntegrityChecker(scannedFileList, manifest);
        List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we have the expected results
        assertThat(results).containsExactlyInAnyOrderElementsOf(expectedResults);
    }
}
