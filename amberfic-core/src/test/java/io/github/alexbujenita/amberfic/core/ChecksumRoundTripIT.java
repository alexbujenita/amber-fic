package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChecksumRoundTripIT {

    @Test
    void preservesEntriesInFilenameOrderAcrossRoundTrip() throws IOException {
        // given a list of ChecksumEntry and a checksum writer
        final List<ChecksumEntry> entries =
                List.of(new ChecksumEntry("hash_123", "sub/b.png"), new ChecksumEntry("hash_456", "a.png"));
        final StringWriter out = new StringWriter();
        final ChecksumWriter checksumWriter = new ChecksumWriter(entries);

        // when we write the results
        checksumWriter.write(out);

        // and we pass its output to the checksum reader
        StringReader reader = new StringReader(out.toString());
        final ChecksumReader checksumReader = new ChecksumReader();
        List<ChecksumEntry> actualEntries = checksumReader.parseChecksum(reader);

        // then the actual entries are correctly in order
        final ChecksumEntry entry1 = new ChecksumEntry("hash_456", "a.png");
        final ChecksumEntry entry2 = new ChecksumEntry("hash_123", "sub/b.png");

        assertThat(actualEntries).containsExactly(entry1, entry2);
    }
}
