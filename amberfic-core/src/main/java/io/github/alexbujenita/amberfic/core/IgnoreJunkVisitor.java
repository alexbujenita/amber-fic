package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.List;

// TODO: rename this once it has more functionality
class IgnoreJunkVisitor extends SimpleFileVisitor<Path> {

    private final List<ScannedFile> scannedFiles;
    private final List<IgnoredEntry> ignoredEntries;
    private final Path rootPath;

    public IgnoreJunkVisitor(List<ScannedFile> scannedFiles, List<IgnoredEntry> ignoredEntries, Path rootPath) {
        this.scannedFiles = scannedFiles;
        this.ignoredEntries = ignoredEntries;
        this.rootPath = rootPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        final Path dirName = dir.getFileName();
        if (null == dirName) {
            // we must be at a system root level, further checks will have throw an NPE
            return FileVisitResult.CONTINUE;
        }
        if (JunkFilter.isJunkFolder(dirName) || Files.isHidden(dir)) {
            this.ignoredEntries.add(new IgnoredEntry(dir, getStandardizedPath(this.rootPath, dir)));
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && !JunkFilter.isJunk(file) && !Files.isHidden(file)) {
            this.scannedFiles.add(new ScannedFile(file, getStandardizedPath(this.rootPath, file)));
        } else {
            this.ignoredEntries.add(new IgnoredEntry(file, getStandardizedPath(this.rootPath, file)));
        }

        return FileVisitResult.CONTINUE;
    }

    /**
     * Helps standardize a path to a file. Windows backslash folder\file.txt vs Mac/Linux forward slash folder/file.txt
     *
     * @param path The root
     * @param file The current file
     * @return A standardized path to the file
     */
    private String getStandardizedPath(Path path, Path file) {
        final Path relativePath = path.relativize(file);
        final StringBuilder fullPath = new StringBuilder();
        for (Path component : relativePath) {
            if (!fullPath.isEmpty()) {
                fullPath.append("/");
            }
            fullPath.append(component);
        }

        return Normalizer.normalize(fullPath.toString(), Normalizer.Form.NFC);
    }
}
