package io.github.alexbujenita.amberfic.cli;

import io.github.alexbujenita.amberfic.core.ManifestAlreadyExistsException;
import picocli.CommandLine;

class ExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    private static final String MANIFEST_EXISTS_EX_MESSAGE =
            "Create cannot overwrite an existing manifest, either call update or delete the manifest manually.";

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult fullParseResult)
            throws Exception {
        if (ex instanceof ManifestAlreadyExistsException) {
            cmd.getErr().println(MANIFEST_EXISTS_EX_MESSAGE);
            return 2;
        }
        throw ex;
    }
}
