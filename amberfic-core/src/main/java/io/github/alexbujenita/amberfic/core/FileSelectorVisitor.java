package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.List;

/**
 * A class that selects what files should be hashed.
 */
class FileSelectorVisitor extends SimpleFileVisitor<Path> {

    /** List to be populated with files to be hashed */
    private final List<ScannedFile> scannedFiles;
    /** List to be populated with files to be ignored */
    private final List<IgnoredEntry> ignoredEntries;
    /** The path from where we start the scan */
    private final Path rootPath;
    /** If this is true, hidden folders will be traversed and hidden files will be reported for hashing */
    private final boolean includeHidden;

    public FileSelectorVisitor(
            List<ScannedFile> scannedFiles, List<IgnoredEntry> ignoredEntries, Path rootPath, boolean includeHidden) {
        this.scannedFiles = scannedFiles;
        this.ignoredEntries = ignoredEntries;
        this.rootPath = rootPath;
        this.includeHidden = includeHidden;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        final Path dirName = dir.getFileName();
        if (null == dirName) {
            // we must be at a system root level, further checks will have throw an NPE
            return FileVisitResult.CONTINUE;
        }
        if (shouldIgnoreDir(dir, dirName)) {
            this.ignoredEntries.add(new IgnoredEntry(dir, getStandardizedPath(this.rootPath, dir)));
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean shouldIgnoreDir(Path dir, Path dirName) throws IOException {
        if (this.includeHidden) {
            return JunkFilter.isJunkFolder(dirName);
        }
        return JunkFilter.isJunkFolder(dirName) || Files.isHidden(dir);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (shouldIgnoreFile(file, attrs)) {
            this.scannedFiles.add(new ScannedFile(file, getStandardizedPath(this.rootPath, file)));
        } else {
            this.ignoredEntries.add(new IgnoredEntry(file, getStandardizedPath(this.rootPath, file)));
        }

        return FileVisitResult.CONTINUE;
    }

    private boolean shouldIgnoreFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (this.includeHidden) {
            return attrs.isRegularFile() && !JunkFilter.isJunk(file);
        }
        return attrs.isRegularFile() && !JunkFilter.isJunk(file) && !Files.isHidden(file);
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
