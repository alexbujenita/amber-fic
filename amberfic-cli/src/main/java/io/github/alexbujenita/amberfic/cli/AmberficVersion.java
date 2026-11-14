package io.github.alexbujenita.amberfic.cli;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import picocli.CommandLine;

public class AmberficVersion implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        try (InputStream readProps = Objects.requireNonNull(
                AmberficVersion.class.getResourceAsStream("/amberfic-version.properties"),
                "Check `amberfic-version.properties`, file not found!")) {
            final Properties props = new Properties();
            props.load(readProps);
            final String version = props.getProperty("version");
            return new String[] {"amberfic " + version};
        }
    }
}
