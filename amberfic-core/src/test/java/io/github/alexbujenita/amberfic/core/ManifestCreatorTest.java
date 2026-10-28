package io.github.alexbujenita.amberfic.core;

import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_256;
import static io.github.alexbujenita.amberfic.core.util.Constants.ABC_SHA_512;
import static io.github.alexbujenita.amberfic.core.util.Constants.EMPTY_SHA_256;
import static io.github.alexbujenita.amberfic.core.util.Constants.EMPTY_SHA_512;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ManifestCreatorTest {

    @Test
    void producesAnUpdatedManifest() {
        // given an existing manifest, a file missing from the scan, and a new file that was scanned
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(EMPTY_SHA_256, "old.jpg"),
                        new ChecksumEntry(EMPTY_SHA_256, "miss_in_scan.jpg")),
                HashAlgorithm.SHA256);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromScan("miss_in_scan.jpg"),
                new IntegrityCheckResult.MissingFromManifest("new.jpg", ABC_SHA_256));

        // when we create the new manifest
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest, integrityCheckResults, new ManifestCreator.UpdateOptions(false));

        // then the new one contains the correct entries
        final List<ChecksumEntry> expectedEntries = List.of(
                new ChecksumEntry(EMPTY_SHA_256, "old.jpg"),
                new ChecksumEntry(EMPTY_SHA_256, "miss_in_scan.jpg"),
                new ChecksumEntry(ABC_SHA_256, "new.jpg"));

        assertThat(updatedManifest.algorithm()).isSameAs(HashAlgorithm.SHA256);
        assertThat(updatedManifest.entries()).containsExactlyInAnyOrderElementsOf(expectedEntries);
    }

    @Test
    void replacesHashOnlyForAcceptedFailedPaths() {
        // given an existing manifest with old hashes
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry("initially_good_hash_file_corrupted", "corrupted.jpg"),
                        new ChecksumEntry(
                                "initially_good_hash_but_file_intentionally_changed_by_user", "changed_shadows.jpg")),
                HashAlgorithm.SHA512);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Failed("corrupted.jpg", "new_hash_for_corrupted"),
                new IntegrityCheckResult.Failed("changed_shadows.jpg", "new_hash_for_changed_shadows"));

        // when we create the new manifest, and we specify which path we want to accept as new
        final Set<String> acceptedPaths = Set.of("changed_shadows.jpg");
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest, integrityCheckResults, new ManifestCreator.UpdateOptions(false, acceptedPaths));

        // then the new one contains the expected entries
        final List<ChecksumEntry> expectedEntries = List.of(
                // we don't update this!
                new ChecksumEntry("initially_good_hash_file_corrupted", "corrupted.jpg"),
                // this instead is updated as it's in the accepted set
                new ChecksumEntry("new_hash_for_changed_shadows", "changed_shadows.jpg"));

        assertThat(updatedManifest.algorithm()).isSameAs(HashAlgorithm.SHA512);
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
                new IntegrityCheckResult.Failed("failed.jpg", "irrelevant"),
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

    @Test
    void updateIsIdempotent() throws IOException {
        // given an existing manifest, a file missing from the scan, and a new file that was scanned
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(EMPTY_SHA_256, "old.jpg"),
                        new ChecksumEntry(EMPTY_SHA_256, "miss_in_scan.jpg")),
                HashAlgorithm.SHA256);
        final List<IntegrityCheckResult> integrityCheckResultsFirstPass = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromScan("miss_in_scan.jpg"),
                new IntegrityCheckResult.MissingFromManifest("new.jpg", ABC_SHA_256));

        // when we create the new manifest
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest, integrityCheckResultsFirstPass, new ManifestCreator.UpdateOptions(false));

        // and once we write this manifest
        final StringWriter outFirstPass = new StringWriter();
        final ChecksumWriter checksumWriter = new ChecksumWriter(updatedManifest.entries());
        checksumWriter.write(outFirstPass);

        // and we call update again with the updated manifest
        final List<IntegrityCheckResult> integrityCheckResultsSecondPass = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromScan("miss_in_scan.jpg"),
                new IntegrityCheckResult.Passed("new.jpg"));
        final ChecksumManifest updatedSecondPassManifest = manifestCreator.updateManifest(
                updatedManifest, integrityCheckResultsSecondPass, new ManifestCreator.UpdateOptions(false));

        // then the written manifest is identical to the first one
        final StringWriter outSecondPass = new StringWriter();
        final ChecksumWriter checksumWriter2 = new ChecksumWriter(updatedSecondPassManifest.entries());
        checksumWriter2.write(outSecondPass);

        assertThat(outSecondPass).hasToString(outFirstPass.toString());
    }

    @Test
    void prunesMissingAndReplacesAcceptedHashTogether() {
        // given an existing manifest, a file missing from the scan,
        //  a new file that was scanned and an accepted new hash
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(EMPTY_SHA_256, "old.jpg"),
                        new ChecksumEntry(EMPTY_SHA_256, "miss_in_scan.jpg"),
                        new ChecksumEntry("old_hash", "shadows.jpg")),
                HashAlgorithm.SHA256);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Passed("old.jpg"),
                new IntegrityCheckResult.MissingFromScan("miss_in_scan.jpg"),
                new IntegrityCheckResult.MissingFromManifest("new.jpg", ABC_SHA_256),
                new IntegrityCheckResult.Failed("shadows.jpg", "new_hash"));

        // when we create the new manifest
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest, integrityCheckResults, new ManifestCreator.UpdateOptions(true, Set.of("shadows.jpg")));

        final ChecksumManifest expectedManifest = new ChecksumManifest(
                List.of(
                        new ChecksumEntry(EMPTY_SHA_256, "old.jpg"),
                        new ChecksumEntry("new_hash", "shadows.jpg"),
                        new ChecksumEntry(ABC_SHA_256, "new.jpg")),
                HashAlgorithm.SHA256);

        // then we have the expected outcome
        assertThat(updatedManifest.entries()).containsExactlyInAnyOrderElementsOf(expectedManifest.entries());
        assertThat(updatedManifest.algorithm()).isSameAs(manifest.algorithm());
    }

    @Test
    void replacementsAreAppliedOnFailedOnly() {
        // given an existing manifest
        final ChecksumManifest manifest = new ChecksumManifest(
                List.of(new ChecksumEntry(EMPTY_SHA_256, "old.jpg"), new ChecksumEntry(EMPTY_SHA_256, "gone.jpg")),
                HashAlgorithm.SHA256);
        final List<IntegrityCheckResult> integrityCheckResults = List.of(
                new IntegrityCheckResult.Passed("old.jpg"), new IntegrityCheckResult.MissingFromScan("gone.jpg"));

        // when we create the new manifest, without pruning
        final ManifestCreator manifestCreator = new ManifestCreator();
        final ChecksumManifest updatedManifest = manifestCreator.updateManifest(
                manifest,
                integrityCheckResults,
                new ManifestCreator.UpdateOptions(false, Set.of("old.jpg", "typo.jpg", "gone.jpg")));

        final ChecksumManifest expectedManifest = new ChecksumManifest(
                List.of(new ChecksumEntry(EMPTY_SHA_256, "old.jpg"), new ChecksumEntry(EMPTY_SHA_256, "gone.jpg")),
                HashAlgorithm.SHA256);

        // then we have the expected outcome
        assertThat(updatedManifest.entries()).containsExactlyInAnyOrderElementsOf(expectedManifest.entries());
        assertThat(updatedManifest.algorithm()).isSameAs(manifest.algorithm());
    }
}
