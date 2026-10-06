package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileWalkerTest {

    @Test
    void walk(@TempDir Path tempDir) throws IOException {

        // given a root dir and a nested dir
        final Path rootPath = tempDir.resolve("root");
        final Path nested = rootPath.resolve("nestedA");

        Files.createDirectories(nested);

        // and some files in it
        final String raFileName = "r_a.png";
        final String nestedFileName = "nested_a.png";
        Files.createFile(rootPath.resolve(raFileName));
        Files.createFile(nested.resolve(nestedFileName));

        final FileWalker walker = new FileWalker();

        // when we walk al the dirs
        List<ScannedFile> files = walker.walk(rootPath);

        // then we collect all the files with their relative name from the root dir
        final String expectedRelativeFileName = nested.getFileName().toString() + "/" + nestedFileName;
        assertThat(files)
                .isNotNull()
                .extracting(ScannedFile::relativeName)
                .containsExactlyInAnyOrder(raFileName, expectedRelativeFileName);
        ;
    }
}
