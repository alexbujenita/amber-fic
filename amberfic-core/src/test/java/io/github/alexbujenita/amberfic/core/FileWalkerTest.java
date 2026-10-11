package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FileWalkerTest {

    static String[] OS_JUNK_FILES = {
        ".DS_Store",
        ".fseventsd",
        "Thumbs.db",
        "ehthumbs.db",
        "ehthumbs_vista.db",
        ".directory",
        "desktop.ini",
        "._z.png"
    };

    static Configuration windowsConfig = Configuration.windows().toBuilder()
            .setAttributeViews("basic", "dos")
            .build();

    static Stream<Arguments> osFsProvider() {
        return Stream.of(
                arguments("MacOS", Configuration.osX()),
                arguments("UNIX", Configuration.unix()),
                arguments("Windows", Configuration.windows()));
    }

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
        Results results = walker.walk(rootPath);

        // then we collect all the files with their relative name from the root dir
        final List<ScannedFile> scannedResults = results.scannedFiles();
        final String expectedRelativeFileName = nested.getFileName().toString() + "/" + nestedFileName;
        assertThat(scannedResults)
                .isNotNull()
                .extracting(ScannedFile::relativeName)
                .containsExactlyInAnyOrder(raFileName, expectedRelativeFileName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void returnsOnlyRegularFilesWithoutFollowingSymlinks(String name, Configuration configuration) throws IOException {
        // given a root dir a file with a broken symlink
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path tempDir = jimfs.createTempDirectory();
            final Path rootPath = tempDir.resolve("root");
            Files.createDirectories(rootPath);

            final String fileName = "photo.png";
            Files.createFile(rootPath.resolve(fileName));

            Files.createSymbolicLink(rootPath.resolve("r_a.png"), rootPath.resolve(fileName));
            Files.createSymbolicLink(rootPath.resolve("r_b.png"), rootPath.resolve("nope.png"));

            final FileWalker walker = new FileWalker();

            // when we walk all the dirs
            final Results results = walker.walk(rootPath);

            // then we only get the real file
            final List<ScannedFile> scannedResults = results.scannedFiles();
            assertThat(scannedResults)
                    .isNotNull()
                    .extracting(ScannedFile::relativeName)
                    .containsExactlyInAnyOrder(fileName);
        }
    }

    static final class JimfsFixture implements AutoCloseable {

        private final FileSystem fileSystem;
        private final Path root;

        JimfsFixture(Configuration configuration) {
            this.fileSystem = Jimfs.newFileSystem(configuration);
            this.root = this.fileSystem.getRootDirectories().iterator().next();
        }

        public Path createTempDirectory() throws IOException {
            return Files.createTempDirectory(fileSystem.getPath(this.root.toString()), "junit-");
        }

        @Override
        public void close() throws IOException {
            fileSystem.close();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void itReturnsFilesSortedByName(String name, Configuration configuration) throws IOException {
        // given a root dir and sub dirs with files
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path tempDir = jimfs.createTempDirectory();
            final Path rootPath = tempDir.resolve("root");
            final Path nested = rootPath.resolve("a");
            Files.createDirectories(nested);

            Files.createFile(rootPath.resolve("b.png"));
            Files.createFile(rootPath.resolve("a.png"));
            Files.createFile(nested.resolve("z.png"));
            Files.createFile(nested.resolve("abc.png"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then the files are sorted in alphabetic order
            final List<ScannedFile> scannedResults = results.scannedFiles();
            assertThat(scannedResults)
                    .extracting(ScannedFile::relativeName)
                    .containsExactly("a.png", "a/abc.png", "a/z.png", "b.png");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shouldIgnoreOsJunkFiles(String name, Configuration configuration) throws IOException {
        // given a root dir and sub dirs with files and some junk files
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path tempDir = jimfs.createTempDirectory();
            final Path rootPath = tempDir.resolve("root");
            final Path nested = rootPath.resolve("a");
            Files.createDirectories(nested);

            Files.createFile(rootPath.resolve("b.png"));
            Files.createFile(rootPath.resolve("keep_me.db"));
            Files.createFile(nested.resolve("z.png"));
            Files.createFile(nested.resolve("._old_file.txt"));

            createJunkFiles(List.of(rootPath, nested));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we don't include the OS' junk files
            final List<ScannedFile> scannedResults = results.scannedFiles();
            assertThat(scannedResults)
                    .extracting(ScannedFile::relativeName)
                    .containsExactly("a/z.png", "b.png", "keep_me.db");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shouldIgnoreOsJunkFolder(String name, Configuration configuration) throws IOException {
        // given a root dir and sub dirs with files and some junk files
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path tempDir = jimfs.createTempDirectory();
            final Path rootPath = tempDir.resolve("root");
            final Path nested = rootPath.resolve(".fseventsd");
            Files.createDirectories(nested);

            Files.createFile(nested.resolve("bs.png"));
            Files.createFile(rootPath.resolve("abc.png"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we don't include the OS' junk files
            final List<ScannedFile> scannedResults = results.scannedFiles();
            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("abc.png");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void walksFilesystemRoot(String name, Configuration configuration) throws IOException {
        // given a system root dir with a file
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path rootPath = jimfs.root;

            Files.createFile(rootPath.resolve("abc.png"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we still have the correct file without throwing
            final List<ScannedFile> scannedResults = results.scannedFiles();
            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("abc.png");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shoudlIncludeIgnoredEntriesFolders(String name, Configuration configuration) throws IOException {
        // given a system root dir with a file and an ignored dir
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("holiday");
            final Path junkFolder = nested.resolve(".fseventsd");
            final Path nestedInJunkFolder = junkFolder.resolve("party");

            Files.createFile(rootPath.resolve("photo.png"));
            Files.createDirectories(nestedInJunkFolder);
            Files.createFile(junkFolder.resolve("ghost.png"));
            Files.createFile(nestedInJunkFolder.resolve("ignored_again.txt"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we have the correct file and the correct ignored entry, we don't count anything since we don't
            // traverse it
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();
            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("photo.png");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder("holiday/.fseventsd");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shoudlIncludeIgnoredSysFolders(String name, Configuration configuration) throws IOException {
        // given a system root dir with a file and system dirs
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path rootPath = jimfs.root;
            final String[] sysDirs = {".Spotlight-V100", ".Trashes", "System Volume Information", "$RECYCLE.BIN"};

            for (String sysDir : sysDirs) {
                Path junk = rootPath.resolve(sysDir);
                Files.createDirectories(junk);
                Files.createFile(junk.resolve("cant_be_seen.jpg"));
            }

            Files.createFile(rootPath.resolve("photo.png"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we have the correct file and the correct ignored entry, we don't count anything since we don't
            // traverse it
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();
            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("photo.png");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder(
                            ".Spotlight-V100", ".Trashes", "System Volume Information", "$RECYCLE.BIN");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shoudlIncludeIgnoredEntriesFiles(String name, Configuration configuration) throws IOException {
        // given a system root dir with a file and an ignored file
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("holiday");

            Files.createDirectories(nested);

            Files.createFile(nested.resolve("notes.db"));
            Files.createFile(nested.resolve("Thumbs.db"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we have the correct file and the correct ignored entry
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();
            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("holiday/notes.db");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder("holiday/Thumbs.db");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shoudlIncludeIgnoredEntriesSymlinks(String name, Configuration configuration) throws IOException {
        // given a system root dir with a file and some symlinks
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("holiday");

            Files.createDirectories(nested);

            Files.createFile(nested.resolve("yo.gif"));
            Files.createFile(nested.resolve("Jo.xls"));

            // valid
            Files.createSymbolicLink(nested.resolve("Mama.xls"), nested.resolve("Jo.xls"));
            // invalid
            Files.createSymbolicLink(nested.resolve("broken.gif"), nested.resolve("nothing_here.asm"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // then we have the correct file and the correct ignored entry
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();
            assertThat(scannedResults)
                    .extracting(ScannedFile::relativeName)
                    .containsExactly("holiday/Jo.xls", "holiday/yo.gif");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder("holiday/Mama.xls", "holiday/broken.gif");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("osFsProvider")
    void shouldIgnoreProgramGeneratedChecksum(String name, Configuration configuration) throws IOException {
        // given a system root dir with some files and a final generated checksum
        try (final JimfsFixture jimfs = new JimfsFixture(configuration)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");

            Files.createDirectories(nested);

            Files.createFile(nested.resolve("photo.jpg"));
            Files.createFile(nested.resolve("amberfic_checksum.sha512"));
            Files.createFile(nested.resolve("notes.sha512"));
            Files.createFile(nested.resolve("amberfic_checksum.sha512.tmp"));
            Files.createFile(nested.resolve("notes.tmp"));
            Files.createFile(nested.resolve("amberfic_checksum.sha512.bak"));
            Files.createFile(nested.resolve("notes.bak"));

            Files.createFile(nested.resolve("notes.sha256"));
            Files.createFile(nested.resolve("amberfic_checksum.sha256"));
            Files.createFile(nested.resolve("amberfic_checksum.sha256.tmp"));
            Files.createFile(nested.resolve("amberfic_checksum.sha256.bak"));

            // when we walk all the dirs
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested);

            // then we have the correct files and the ignored generated checksum
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults)
                    .extracting(ScannedFile::relativeName)
                    .containsExactly("notes.bak", "notes.sha256", "notes.sha512", "notes.tmp", "photo.jpg");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder(
                            "amberfic_checksum.sha512",
                            "amberfic_checksum.sha512.tmp",
                            "amberfic_checksum.sha512.bak",
                            "amberfic_checksum.sha256",
                            "amberfic_checksum.sha256.tmp",
                            "amberfic_checksum.sha256.bak");
        }
    }

    @Test
    void hiddenFilesAreIgnored() throws IOException {
        // Given a root directory and 2 files, 1 hidden
        try (final JimfsFixture jimfs = new JimfsFixture(Configuration.unix())) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");
            Files.createDirectories(nested);

            Files.createFile(nested.resolve("IMG_001.jpg"));
            Files.createFile(nested.resolve(".notes.txt"));

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested);

            // Then the scanned results contain the normal file and the ignored hidden file
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("IMG_001.jpg");
            assertThat(ignoredEntries).extracting(IgnoredEntry::relativeName).containsExactlyInAnyOrder(".notes.txt");
        }
    }

    @Test
    void hiddenFilesAreIgnored_Windows() throws IOException {
        // Given a root directory and 2 files, 1 hidden
        try (final JimfsFixture jimfs = new JimfsFixture(windowsConfig)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");
            final Path toBeHidden = nested.resolve("notes.txt");
            Files.createDirectories(nested);

            Files.createFile(nested.resolve("IMG_001.jpg"));
            Files.createFile(toBeHidden);

            Files.setAttribute(toBeHidden, "dos:hidden", true);

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested);

            // Then the scanned results contain the normal file and the ignored hidden file
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("IMG_001.jpg");
            assertThat(ignoredEntries).extracting(IgnoredEntry::relativeName).containsExactlyInAnyOrder("notes.txt");
        }
    }

    @Test
    void hiddenDirsAreIgnored() throws IOException {
        // Given a root directory and 1 file, 1 hidden dir with random stuff
        try (final JimfsFixture jimfs = new JimfsFixture(Configuration.unix())) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");
            Files.createDirectories(nested);
            final Path hiddenDir = Files.createDirectories(nested.resolve(".archive"));
            final Path normalInHiddenDir = Files.createDirectories(hiddenDir.resolve("normalInHidden"));

            Files.createFile(nested.resolve("IMG_001.jpg"));
            Files.createFile(normalInHiddenDir.resolve("hello.txt"));
            Files.createFile(hiddenDir.resolve("something.txt"));

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested);

            // Then the scanned results contain the normal file and the ignored hidden dir
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("IMG_001.jpg");
            assertThat(ignoredEntries).extracting(IgnoredEntry::relativeName).containsExactlyInAnyOrder(".archive");
        }
    }

    @Test
    void hiddenDirsAreIgnored_Windows() throws IOException {
        // Given a root directory and 1 file, 1 hidden dir with random stuff (Windows)
        try (final JimfsFixture jimfs = new JimfsFixture(windowsConfig)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");
            Files.createDirectories(nested);
            final Path hiddenDir = Files.createDirectories(nested.resolve("archive"));
            final Path normalInHiddenDir = Files.createDirectories(hiddenDir.resolve("normalInHidden"));

            Files.createFile(nested.resolve("IMG_001.jpg"));
            Files.createFile(normalInHiddenDir.resolve("hello.txt"));
            Files.createFile(hiddenDir.resolve("something.txt"));

            Files.setAttribute(hiddenDir, "dos:hidden", true);

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested);

            // Then the scanned results contain the normal file and the ignored hidden folder/dir
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("IMG_001.jpg");
            assertThat(ignoredEntries).extracting(IgnoredEntry::relativeName).containsExactlyInAnyOrder("archive");
        }
    }

    @Test
    void hiddenFilesAreFoundInHiddenDirsWithFlag() throws IOException {
        // Given a root directory and some hidden folders and files
        try (final JimfsFixture jimfs = new JimfsFixture(Configuration.unix())) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");
            final Path hiddenDir = Files.createDirectories(nested.resolve(".archive"));
            Files.createDirectories(hiddenDir);

            Files.createFile(nested.resolve("IMG_001.jpg"));
            Files.createFile(hiddenDir.resolve(".notes.txt"));
            Files.createFile(hiddenDir.resolve("not_hidden.txt"));
            Files.createFile(hiddenDir.resolve(".DS_Store"));

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested, true);

            // Then the scanned results contains all files but still ignores junk files
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults)
                    .extracting(ScannedFile::relativeName)
                    .containsExactly(".archive/.notes.txt", ".archive/not_hidden.txt", "IMG_001.jpg");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder(".archive/.DS_Store");
        }
    }

    @Test
    void hiddenFilesAreFoundInHiddenDirsWithFlag_Windows() throws IOException {
        // Given a root directory and 2 files, 1 hidden
        try (final JimfsFixture jimfs = new JimfsFixture(windowsConfig)) {
            final Path rootPath = jimfs.root;
            final Path nested = rootPath.resolve("photos");
            final Path hiddenDir = Files.createDirectories(nested.resolve(".archive"));
            Files.createDirectories(hiddenDir);

            Files.createFile(nested.resolve("IMG_001.jpg"));
            final Path hiddenFile = Files.createFile(hiddenDir.resolve("notes.txt"));
            Files.createFile(hiddenDir.resolve("not_hidden.txt"));
            Files.createFile(hiddenDir.resolve(".DS_Store"));

            Files.setAttribute(hiddenDir, "dos:hidden", true);
            Files.setAttribute(hiddenFile, "dos:hidden", true);

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(nested, true);

            // Then the scanned results contains all files but still ignores junk files
            final List<ScannedFile> scannedResults = results.scannedFiles();
            final List<IgnoredEntry> ignoredEntries = results.ignored();

            assertThat(scannedResults)
                    .extracting(ScannedFile::relativeName)
                    .containsExactly(".archive/not_hidden.txt", ".archive/notes.txt", "IMG_001.jpg");
            assertThat(ignoredEntries)
                    .extracting(IgnoredEntry::relativeName)
                    .containsExactlyInAnyOrder(".archive/.DS_Store");
        }
    }

    @Test
    void fileNamesNormalised() throws IOException {
        // Given a root directory and a file using NFD (Decomposed) Unicode for the accented 'é' character (café)
        try (final JimfsFixture jimfs = new JimfsFixture(Configuration.unix())) {
            final Path rootPath = jimfs.root;

            Path caffe = rootPath.resolve("cafe\u0301.jpg");

            Files.createFile(caffe);

            // when we walk the dir
            final FileWalker walker = new FileWalker();
            final Results results = walker.walk(rootPath);

            // Then the scanned results contain the path and the normalized NFC filename (café.jpg)
            final List<ScannedFile> scannedResults = results.scannedFiles();

            assertThat(scannedResults).extracting(ScannedFile::path).containsExactly(caffe);
            assertThat(scannedResults).extracting(ScannedFile::relativeName).containsExactly("caf\u00E9.jpg");
        }
    }

    private void createJunkFiles(List<Path> paths) throws IOException {
        for (Path path : paths) {
            for (String junkFileName : OS_JUNK_FILES) {
                Files.createFile(path.resolve(junkFileName));
            }
        }
    }

    @Test
    void throwsExceptionForDirNotFound(@TempDir Path tempDir) {
        // Given a non existing path
        final Path rootPath = tempDir.resolve("random");
        final FileWalker walker = new FileWalker();

        // when we call the walk method then it throws an exception

        assertThatThrownBy(() -> walker.walk(rootPath)).isInstanceOf(NoSuchFileException.class);
    }
}
