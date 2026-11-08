module io.github.alexbujenita.amberfic.cli {
    requires io.github.alexbujenita.amberfic.core;
    requires info.picocli;

    opens io.github.alexbujenita.amberfic.cli to
            info.picocli;
}
