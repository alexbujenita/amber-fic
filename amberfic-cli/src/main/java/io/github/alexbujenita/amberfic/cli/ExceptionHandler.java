package io.github.alexbujenita.amberfic.cli;

import io.github.alexbujenita.amberfic.core.ManifestAlreadyExistsException;
import io.github.alexbujenita.amberfic.core.SourceDirectoryNotFoundException;
import picocli.CommandLine;

class ExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    private static final String MANIFEST_EXISTS_EX_MESSAGE =
            "Create cannot overwrite an existing manifest, either call update or delete the manifest manually.";

    private static final String MISSING_SOURCE_DIR_EX_MESSAGE =
            "The passed directory doesn't exist, make sure you typed the correct path.";

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult fullParseResult)
            throws Exception {
        return switch (ex) {
            case ManifestAlreadyExistsException _ -> {
                cmd.getErr().println(MANIFEST_EXISTS_EX_MESSAGE);
                yield 2;
            }
            case SourceDirectoryNotFoundException sdnfe -> {
                cmd.getErr().println(MISSING_SOURCE_DIR_EX_MESSAGE + " " + sdnfe.getFile());
                yield 2;
            }
            default -> throw ex;
        };
    }
}
