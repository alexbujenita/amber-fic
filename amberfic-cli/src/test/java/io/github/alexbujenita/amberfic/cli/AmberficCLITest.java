package io.github.alexbujenita.amberfic.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
}
