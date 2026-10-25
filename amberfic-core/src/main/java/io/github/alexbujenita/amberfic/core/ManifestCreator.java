package io.github.alexbujenita.amberfic.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class ManifestCreator {

    record UpdateOptions(boolean prune) {}

    /**
     * <p>As users add new files those will be missing from the original manifest.</p>
     * <p> Returns a new manifest: the existing entries, plus the files missing from the manifest.</p>
     * <p>The idea is to merge those new entries in a new manifest.</p>
     * <p>We keep the entries from the original manifest, unless the passed options alters the behaviour, and we add the new files scanned to it with their hash.</p>
     * @param manifest the initial manifest
     * @param integrityCheckResults the results after a pass which also includes the hash for any files missing from the manifest
     * @param options changes the behaviour of what entries get updated
     * when {@code prune} is true, entries whose file is missing from the scan are removed
     * @see io.github.alexbujenita.amberfic.core.IntegrityChecker#calculateIntegrityResults()
     * @return an updated manifest that can be written
     */
    ChecksumManifest updateManifest(
            ChecksumManifest manifest, List<IntegrityCheckResult> integrityCheckResults, UpdateOptions options) {
        final List<ChecksumEntry> missingFromManifest = integrityCheckResults.stream()
                .filter(IntegrityCheckResult.MissingFromManifest.class::isInstance)
                .map(IntegrityCheckResult.MissingFromManifest.class::cast)
                .map(result -> new ChecksumEntry(result.hash(), result.filePath()))
                .toList();

        final Set<String> missingFromScan = integrityCheckResults.stream()
                .filter(IntegrityCheckResult.MissingFromScan.class::isInstance)
                .map(IntegrityCheckResult.MissingFromScan.class::cast)
                .map(IntegrityCheckResult.MissingFromScan::filePath)
                .collect(Collectors.toSet());

        final List<ChecksumEntry> updatedManifestEntries = new ArrayList<>(manifest.entries());
        updatedManifestEntries.addAll(missingFromManifest);

        if (options.prune()) {
            updatedManifestEntries.removeIf(manifestEntry -> missingFromScan.contains(manifestEntry.filePath()));
        }
        return new ChecksumManifest(List.copyOf(updatedManifestEntries), manifest.algorithm());
    }
}
