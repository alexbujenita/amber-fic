package io.github.alexbujenita.amberfic.cli;

import io.github.alexbujenita.amberfic.core.Amberfic;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "create", description = "Create a checksum manifest from a given folder.")
public class CreateCommand implements Callable<Integer> {

    @CommandLine.Parameters(paramLabel = "<folder>", description = "Folder used as base to start scanning all files.")
    private Path folder;

    @Override
    public Integer call() throws IOException {
        Amberfic.create(folder);
        return 0;
    }
}
