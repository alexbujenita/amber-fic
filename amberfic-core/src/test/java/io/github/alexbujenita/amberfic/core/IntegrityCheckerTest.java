package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_512;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
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

    static final String ABC_SHA_256 = "b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78";
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
                                new IntegrityCheckResult.MissingFromManifest("extra.jpg", ABC_SHA_512))),
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
                                new IntegrityCheckResult.MissingFromManifest("renamed.jpg", ABC_SHA_512))),
                argumentSet(
                        "move to sub folder",
                        List.of("sub/photo.jpg"),
                        new ChecksumManifest(
                                List.of(new ChecksumEntry(ABC_SHA_512, "photo.jpg")), HashAlgorithm.SHA512),
                        List.of(
                                new IntegrityCheckResult.MissingFromScan("photo.jpg"),
                                new IntegrityCheckResult.MissingFromManifest("sub/photo.jpg", ABC_SHA_512))));
    }

    static Stream<Arguments> algorithmDigestProvider() {
        return Stream.of(
                argumentSet("SHA256", HashAlgorithm.SHA256, ABC_SHA_256),
                argumentSet("SHA512", HashAlgorithm.SHA512, ABC_SHA_512));
    }

    static Stream<Arguments> provideUnreadableScenarios() {
        return Stream.of(
                argumentSet("directory", (PathSetup) Files::createDirectory),
                argumentSet("vanished", (PathSetup) _ -> {}));
    }

    @FunctionalInterface
    interface PathSetup {
        void prepare(Path path) throws IOException;
    }

    @ParameterizedTest
    @MethodSource("algorithmDigestProvider")
    void checksIntegrityOfAPreHashedFile(HashAlgorithm algorithm, String digest, @TempDir Path path) throws Exception {
        // Given a file with some content and a checksum manifest
        final String fileName = "photo.jpg";
        final Path photo = Files.createFile(path.resolve(fileName));

        try (BufferedWriter writer = Files.newBufferedWriter(photo)) {
            writer.write(ABC);
        }

        final ScannedFile scannedFile = new ScannedFile(photo, fileName);

        final ChecksumManifest manifest = new ChecksumManifest(List.of(new ChecksumEntry(digest, fileName)), algorithm);

        // when we call the integrity checker
        final IntegrityChecker checker = new IntegrityChecker(List.of(scannedFile), manifest);
        final List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we have a successful result
        assertThat(results).hasSize(1).contains(new IntegrityCheckResult.Passed(fileName));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"ADC", ""})
    void checksIntegrityOfAPreHashedFile_hashMismatchShouldFail(String fileContents, @TempDir Path path)
            throws Exception {
        // Given a file with some content and a checksum manifest that doesn't match the content
        final String fileName = "photo.jpg";
        final Path photo = Files.createFile(path.resolve(fileName));

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

    @ParameterizedTest
    @MethodSource("provideUnreadableScenarios")
    void reportsUnreadableFileAndChecksTheRest(PathSetup setup, @TempDir Path path) throws IOException {
        // given an unreadable file
        final String imageAsDir = "dir_not_file.jpg";
        final Path dirNotFile = path.resolve(imageAsDir);
        setup.prepare(dirNotFile);

        // and a readable one
        final String fileName = "photo.jpg";
        final Path photo = Files.createFile(path.resolve(fileName));

        try (BufferedWriter writer = Files.newBufferedWriter(photo)) {
            writer.write(ABC);
        }

        final ScannedFile unreadable = new ScannedFile(dirNotFile, imageAsDir);
        final ScannedFile scannedFile = new ScannedFile(photo, fileName);

        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(new ChecksumEntry("doesn't_matter", imageAsDir), new ChecksumEntry(ABC_SHA_512, fileName)),
                HashAlgorithm.SHA512);

        // when we call the integrity checker
        final IntegrityChecker checker = new IntegrityChecker(List.of(unreadable, scannedFile), manifest);
        List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we have the correct entries
        assertThat(results)
                .containsExactlyInAnyOrderElementsOf(List.of(
                        new IntegrityCheckResult.Unreadable(imageAsDir), new IntegrityCheckResult.Passed(fileName)));
    }

    @Test
    void rejectsEmptyManifest() {
        // given an empty manifest when we instantiate the integrity checker then we throw an exception
        final ChecksumManifest manifest = new ChecksumManifest(List.of(), HashAlgorithm.SHA512);
        assertThatThrownBy(() -> new IntegrityChecker(List.of(), manifest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void passesWalkedFilesUnderADifferentRootWithTheSameLayout(@TempDir Path tempDir) throws IOException {
        // given the manifest's layout under a root other than the one it was created for
        final Path root = tempDir.resolve("Backup/Photos");
        final Path vacation = Files.createDirectories(root.resolve("vacation"));
        Files.writeString(root.resolve("top.jpg"), ABC);
        Files.writeString(vacation.resolve("beach.jpg"), ABC);

        // and a manifest that stores only root-relative names
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(ABC_SHA_512, "top.jpg"),
                        new ChecksumEntry(ABC_SHA_512, "vacation/beach.jpg")),
                HashAlgorithm.SHA512);

        // when the walked files are checked
        final List<ScannedFile> scannedFiles = new FileWalker().walk(root).scannedFiles();
        final List<IntegrityCheckResult> results =
                new IntegrityChecker(scannedFiles, manifest).calculateIntegrityResults();

        // then every file passes
        assertThat(results)
                .containsExactlyInAnyOrder(
                        new IntegrityCheckResult.Passed("top.jpg"),
                        new IntegrityCheckResult.Passed("vacation/beach.jpg"));
    }
}
