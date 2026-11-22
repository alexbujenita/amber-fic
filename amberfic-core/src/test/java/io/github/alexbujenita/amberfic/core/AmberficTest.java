package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_512;
import static io.github.alexbujenita.amberfic.core.util.Constants.EMPTY_SHA_512;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.alexbujenita.amberfic.core.exceptions.ManifestAlreadyExistsException;
import io.github.alexbujenita.amberfic.core.exceptions.SourceDirectoryNotFoundException;
import io.github.alexbujenita.amberfic.core.exceptions.SourceNotDirectoryException;
import io.github.alexbujenita.amberfic.core.exceptions.StaleTempFileException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Test
    void createThrowsAnErrorIfManifestAlreadyPresent(@TempDir Path tmpDir) throws IOException {
        // Given an existing manifest
        final Path existingManifest = tmpDir.resolve("amberfic_checksum.sha512");
        final String existingManifestContent = "hash  file\n";
        Files.writeString(existingManifest, existingManifestContent);

        // when we call the create method then it throws an error and the manifest isn't touched
        assertThatThrownBy(() -> Amberfic.create(tmpDir)).isInstanceOf(ManifestAlreadyExistsException.class);
        assertThat(existingManifest).hasBinaryContent(existingManifestContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void createThrowsAnErrorIfTmpManifestAlreadyPresent(@TempDir Path tmpDir) throws IOException {
        // Given an existing tmp manifest
        final Path existingTmpManifest = tmpDir.resolve("amberfic_checksum.sha512.tmp");
        final String existingTmpManifestContent = "hash  file\n";
        Files.writeString(existingTmpManifest, existingTmpManifestContent);

        // when we call the create method then it throws an error and the tmp manifest isn't touched
        assertThatThrownBy(() -> Amberfic.create(tmpDir))
                .isInstanceOf(StaleTempFileException.class)
                .hasMessageContaining("amberfic_checksum.sha512.tmp");
        assertThat(existingTmpManifest).hasBinaryContent(existingTmpManifestContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void createThrowsAnErrorIfAnyManifestAlreadyPresentWithManifestTakingPrecedence(@TempDir Path tmpDir)
            throws IOException {
        // Given an existing tmp manifest and a normal manifest
        final Path existingTmpManifest = tmpDir.resolve("amberfic_checksum.sha512.tmp");
        final Path existingManifest = tmpDir.resolve("amberfic_checksum.sha512");
        final String existingManifestContent = "hash  file\n";
        final String existingTmpManifestContent = "hash  tmp\n";
        Files.writeString(existingTmpManifest, existingTmpManifestContent);
        Files.writeString(existingManifest, existingManifestContent);

        // when we call the create method then it throws an error and neither file is touched
        assertThatThrownBy(() -> Amberfic.create(tmpDir)).isInstanceOf(ManifestAlreadyExistsException.class);
        assertThat(existingTmpManifest).hasBinaryContent(existingTmpManifestContent.getBytes(StandardCharsets.UTF_8));
        assertThat(existingManifest).hasBinaryContent(existingManifestContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void createThrowsAnErrorIfDirDoesntExist(@TempDir Path tmpDir) {
        // given / when we call the create method on a non existing dir then it throws an error
        assertThatThrownBy(() -> Amberfic.create(tmpDir.resolve("nowhere")))
                .isInstanceOf(SourceDirectoryNotFoundException.class);
    }

    @Test
    void createThrowsAnErrorIfSourceIsntADir(@TempDir Path tmpDir) throws IOException {
        // given a file
        final Path file = tmpDir.resolve("file.txt");
        Files.writeString(file, "just a txt file\n");

        // when we call the create method on a file instead of a directory then it throws an error
        assertThatThrownBy(() -> Amberfic.create(file)).isInstanceOf(SourceNotDirectoryException.class);
    }
}
