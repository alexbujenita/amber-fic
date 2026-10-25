package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_512;
import static io.github.alexbujenita.amberfic.core.util.Constants.EMPTY_SHA_512;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ManifestCreatorTest {

    @Test
    void producesAnUpdatedManifest() {
        // given an existing manifest and a new file that was scanned
        final ChecksumManifest manifest =
                new ChecksumManifest(List.of(new ChecksumEntry(EMPTY_SHA_512, "old.jpg")), HashAlgorithm.SHA512);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromManifest("new.jpg", ABC_SHA_512));

        // when we create the new manifest
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(manifest, integrityCheckResults);

        // then the new one contains the correct entries
        final List<ChecksumEntry> expectedEntries =
                List.of(new ChecksumEntry(EMPTY_SHA_512, "old.jpg"), new ChecksumEntry(ABC_SHA_512, "new.jpg"));

        assertThat(updatedManifest.algorithm()).isSameAs(manifest.algorithm());
        assertThat(updatedManifest.entries()).containsExactlyInAnyOrderElementsOf(expectedEntries);
    }
}
