package io.github.alexbujenita.amberfic.core;

import java.util.ArrayList;
import java.util.List;

class ManifestCreator {

    /**
     * <p>As users add new files those will be missing from the original manifest.</p>
     * <p>The idea is to merge those new entries in a new manifest.</p>
     * <p>We keep the entries from the original manifest, and we add the new files scanned to it with their hash.</p>
     * @param manifest the initial manifest
     * @param integrityCheckResults the results after a pass which also includes the hash for any files missing from the manifest
     * @see io.github.alexbujenita.amberfic.core.IntegrityChecker#calculateIntegrityResults()
     * @return an updated manifest that can be written
     */
    ChecksumManifest updateManifest(ChecksumManifest manifest, List<IntegrityCheckResult> integrityCheckResults) {
        final List<ChecksumEntry> missingFromManifest = integrityCheckResults.stream()
                .filter(IntegrityCheckResult.MissingFromManifest.class::isInstance)
                .map(IntegrityCheckResult.MissingFromManifest.class::cast)
                .map(result -> new ChecksumEntry(result.hash(), result.filePath()))
                .toList();
        final List<ChecksumEntry> updatedManifestEntries = new ArrayList<>(manifest.entries());
        updatedManifestEntries.addAll(missingFromManifest);
        return new ChecksumManifest(List.copyOf(updatedManifestEntries), manifest.algorithm());
    }
}
