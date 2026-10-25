package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_512;
import static io.github.alexbujenita.amberfic.core.util.Constants.EMPTY_SHA_512;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ManifestCreatorTest {

    @Test
    void producesAnUpdatedManifest() {
        // given an existing manifest, a file missing from the scan, and a new file that was scanned
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(EMPTY_SHA_512, "old.jpg"),
                        new ChecksumEntry(EMPTY_SHA_512, "miss_in_scan.jpg")),
                HashAlgorithm.SHA512);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromScan("miss_in_scan.jpg"),
                new IntegrityCheckResult.MissingFromManifest("new.jpg", ABC_SHA_512));

        // when we create the new manifest
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest, integrityCheckResults, new ManifestCreator.UpdateOptions(false));

        // then the new one contains the correct entries
        final List<ChecksumEntry> expectedEntries = List.of(
                new ChecksumEntry(EMPTY_SHA_512, "old.jpg"),
                new ChecksumEntry(EMPTY_SHA_512, "miss_in_scan.jpg"),
                new ChecksumEntry(ABC_SHA_512, "new.jpg"));

        assertThat(updatedManifest.algorithm()).isSameAs(manifest.algorithm());
        assertThat(updatedManifest.entries()).containsExactlyInAnyOrderElementsOf(expectedEntries);
    }

    @Test
    void producesAnUpdatedManifest_removingMissingFromScan() {
        // given an existing manifest with one entry per check result, including a file missing from the scan
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(EMPTY_SHA_512, "old.jpg"),
                        new ChecksumEntry(EMPTY_SHA_512, "gone.jpg"),
                        new ChecksumEntry(EMPTY_SHA_512, "failed.jpg"),
                        new ChecksumEntry(EMPTY_SHA_512, "locked.jpg")),
                HashAlgorithm.SHA512);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromManifest("new.jpg", ABC_SHA_512),
                new IntegrityCheckResult.MissingFromScan("gone.jpg"),
                new IntegrityCheckResult.Failed("failed.jpg"),
                new IntegrityCheckResult.Unreadable("locked.jpg"));

        // when we create the new manifest
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest, integrityCheckResults, new ManifestCreator.UpdateOptions(true));

        // then the new one contains the correct entries without the missing from scan, keeping the rest
        final List<ChecksumEntry> expectedEntries = List.of(
                new ChecksumEntry(EMPTY_SHA_512, "old.jpg"),
                new ChecksumEntry(ABC_SHA_512, "new.jpg"),
                new ChecksumEntry(EMPTY_SHA_512, "failed.jpg"),
                new ChecksumEntry(EMPTY_SHA_512, "locked.jpg"));

        assertThat(updatedManifest.algorithm()).isSameAs(manifest.algorithm());
        assertThat(updatedManifest.entries()).containsExactlyInAnyOrderElementsOf(expectedEntries);
    }
}
