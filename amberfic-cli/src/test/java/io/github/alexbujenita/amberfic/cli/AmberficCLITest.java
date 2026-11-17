package io.github.alexbujenita.amberfic.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class AmberficCLITest {

    CommandLine cmd;
    StringWriter outWriter;
    StringWriter errorWriter;

    @BeforeEach
    void init() {
        outWriter = new StringWriter();
        errorWriter = new StringWriter();
        cmd = new CommandLine(new AmberficCLI());
        cmd.setOut(new PrintWriter(outWriter));
        cmd.setErr(new PrintWriter(errorWriter));
    }

    @Test
    void printsCorrectVersion() {
        // given we call the amberfic --version
        final int exitCode = AmberficCLI.run(cmd, "--version");
        final String pomVersion = System.getProperty("amberfic-test-version");

        // then the version is correct and no error is produced
        assertThat(pomVersion)
                .as("system property amberfic-test-version from Surefire")
                .isNotBlank();
        assertThat(exitCode).isZero();
        assertThat(outWriter).hasToString("amberfic " + pomVersion + System.lineSeparator());
        assertThat(errorWriter.toString()).isEmpty();
    }

    @Test
    void givenAFolderItCreatesAManifest(@TempDir Path folder) throws IOException {
        // given a folder and a file in it
        final Path testFolder = Files.createDirectories(folder.resolve("test"));
        final Path testFile = testFolder.resolve("a.txt");
        Files.writeString(testFile, "ABC");

        // when we call the create command
        final String[] args = {"create", testFolder.toString()};
        final int exitCode = AmberficCLI.run(cmd, args);

        // then we have a manifest created
        final String expectedManifest =
                "397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119  a.txt\n";
        final String manifest = Files.readString(testFolder.resolve("amberfic_checksum.sha512"));
        assertThat(exitCode).isZero();
        assertThat(manifest).isEqualTo(expectedManifest);
    }

    @Test
    void createShouldFailOnAnExistingChecksumFile(@TempDir Path folder) throws IOException {
        // given a folder, a file in it and an existing manifest
        final Path testFolder = Files.createDirectories(folder.resolve("test"));
        final Path testFile = testFolder.resolve("a.txt");
        final Path existingManifest = testFolder.resolve("amberfic_checksum.sha512");
        Files.writeString(testFile, "ABC");
        final String existingManifestContents = "not_real  a.txt\n";
        Files.writeString(existingManifest, existingManifestContents);

        // when we call the create command
        final String[] args = {"create", testFolder.toString()};
        final int exitCode = AmberficCLI.run(cmd, args);

        // then we have an error as a manifest already exists and the previous manifest is still there
        assertThat(outWriter.toString()).isEmpty();
        assertThat(errorWriter)
                .hasToString(
                        "Create cannot overwrite an existing manifest, either call update or delete the manifest manually."
                                + System.lineSeparator());
        assertThat(exitCode).isEqualTo(2);
        assertThat(existingManifest)
                .binaryContent()
                .containsExactly(existingManifestContents.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void createInformsTheUserIfTheSourceDirDoesntExist(@TempDir Path folder) {
        // given a folder that doesn't exist, when we call the create command
        // then the user is informed of the missing source
        final Path testFolder = folder.resolve("not_there");

        final String[] args = {"create", testFolder.toString()};
        final int exitCode = AmberficCLI.run(cmd, args);

        assertThat(exitCode).isEqualTo(2);
        assertThat(outWriter.toString()).isEmpty();
        assertThat(errorWriter)
                .hasToString("The passed directory doesn't exist, make sure you typed the correct path." + " "
                        + testFolder + System.lineSeparator());
    }
}
