package io.github.alexbujenita.amberfic.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "amberfic",
        mixinStandardHelpOptions = true,
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
