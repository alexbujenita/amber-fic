package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IntegrityCheckerTest {

    @Test
    void checksIntegrityOfAPreHashedFile(@TempDir Path path) throws Exception {
        // Given a file with some content and a checksum manifest
        final String content = "ABC";
        final String fileName = "photo.jpg";
        final Path photo = Files.createFile(path.resolve(fileName));
        final String precomputedHash =
                "397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119";

        try (BufferedWriter writer = Files.newBufferedWriter(photo)) {
            writer.write(content);
        }

        final ScannedFile scannedFile = new ScannedFile(photo, fileName);

        final ChecksumManifest manifest =
                new ChecksumManifest(List.of(new ChecksumEntry(precomputedHash, fileName)), HashAlgorithm.SHA512);

        // when we call the integrity checker
        final IntegrityChecker checker = new IntegrityChecker(List.of(scannedFile), manifest);
        final List<IntegrityCheckResult> results = checker.calculateIntegrityResults();

        // then we have a successful result
        assertThat(results).hasSize(1).contains(new IntegrityCheckResult.Passed(fileName));
    }
}
