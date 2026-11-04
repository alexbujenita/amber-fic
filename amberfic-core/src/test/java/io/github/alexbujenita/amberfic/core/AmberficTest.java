package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_512;
import static io.github.alexbujenita.amberfic.core.util.Constants.EMPTY_SHA_512;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AmberficTest {
    @Test
    void createsAManifest(@TempDir Path tmpDir) throws IOException {
        // Given some files
        final Path sub = tmpDir.resolve("sub");
        final Path zFile = tmpDir.resolve("z.txt");
        final Path subFile = sub.resolve("a.txt");
        Files.createDirectories(sub);

        Files.writeString(zFile, "ABC");
        Files.createFile(subFile);

        // when we call the create method
        Amberfic.create(tmpDir);

        // then we have the correct checksum written
        final Path checksum = tmpDir.resolve("amberfic_checksum.sha512");
        final String checksumContents = Files.readString(checksum);

        assertThat(checksumContents).isEqualTo(EMPTY_SHA_512 + "  sub/a.txt\n" + ABC_SHA_512 + "  z.txt\n");
    }
}
