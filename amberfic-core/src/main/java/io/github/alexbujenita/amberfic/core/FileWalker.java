package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class FileWalker {

    private static final Predicate<Path> IS_REGULAR_FILE_NO_SYM_LINK =
            path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);

    List<ScannedFile> walk(Path path) throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {

            return stream.filter(IS_REGULAR_FILE_NO_SYM_LINK)
                    .map(file -> new ScannedFile(file, getStandardizedPath(path, file)))
                    .toList();
        }
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
        return fullPath.toString();
    }
}
