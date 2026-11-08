package io.github.alexbujenita.amberfic.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "amberfic",
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true,
        description = "Provides utilities to perform checks on files.",
        version = "0.0.1",
        subcommands = {CreateCommand.class})
public class AmberficCLI {

    void main(String... args) {
        int code = run(args);
        System.exit(code);
    }

    static int run(String... args) {
        return run(new CommandLine(new AmberficCLI()), args);
    }

    static int run(CommandLine cmd, String... args) {
        return cmd.execute(args);
    }
}
