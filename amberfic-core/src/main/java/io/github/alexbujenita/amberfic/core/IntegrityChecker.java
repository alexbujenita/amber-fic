package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class IntegrityChecker {
    private final List<IntegrityCheckResult> integrityResults;
    private final List<ScannedFile> scannedFiles;
    private final ChecksumManifest manifest;
    private final Hasher hasher;

    IntegrityChecker(List<ScannedFile> scannedFiles, ChecksumManifest manifest) {
        if (manifest.entries().isEmpty()) {
            throw new IllegalArgumentException("Manifest should contain at least one entry");
        }
        this.scannedFiles = scannedFiles;
        this.manifest = manifest;
        this.integrityResults = new ArrayList<>();
        this.hasher = new Hasher(manifest.algorithm());
    }

    List<IntegrityCheckResult> calculateIntegrityResults() {
        for (ScannedFile fileToCheck : scannedFiles) {

            final String hash;

            try (InputStream inputStream = Files.newInputStream(fileToCheck.path())) {
                hash = hasher.hash(inputStream);
            } catch (final IOException _) {
                // opening, reading, or closing failed: flag this file and check the next one
                this.integrityResults.add(new IntegrityCheckResult.Unreadable(fileToCheck.relativeName()));
                continue;
            }

            final ChecksumEntry entryToCheck = new ChecksumEntry(hash, fileToCheck.relativeName());
            final ChecksumEntry manifestEntry = getManifestEntryByPath(fileToCheck.relativeName());
            addIntegrityResult(entryToCheck, manifestEntry);
        }

        addMissingFromScan(scannedFiles);
        List<IntegrityCheckResult> results = List.copyOf(this.integrityResults);
        this.integrityResults.clear();
        return results;
    }

    /**
     * Checks and adds files that are present in the original manifest but are missing from the scanned source
     *
     * @param scannedFiles Files scanned from the source to check
     */
    private void addMissingFromScan(List<ScannedFile> scannedFiles) {
        final List<String> manifestEntries =
                this.manifest.entries().stream().map(ChecksumEntry::filePath).toList();
        final Set<String> scannedEntries =
                scannedFiles.stream().map(ScannedFile::relativeName).collect(Collectors.toSet());

        manifestEntries.stream()
                .filter(p -> !scannedEntries.contains(p))
                .forEach(p -> this.integrityResults.add(new IntegrityCheckResult.MissingFromScan(p)));
    }

    private void addIntegrityResult(ChecksumEntry entryToCheck, ChecksumEntry manifestEntry) {
        if (manifestEntry == null) {
            integrityResults.add(
                    new IntegrityCheckResult.MissingFromManifest(entryToCheck.filePath(), entryToCheck.hash()));
        } else if (entryToCheck.hash().equals(manifestEntry.hash())) {
            integrityResults.add(new IntegrityCheckResult.Passed(entryToCheck.filePath()));
        } else {
            integrityResults.add(new IntegrityCheckResult.Failed(entryToCheck.filePath()));
        }
    }

    private ChecksumEntry getManifestEntryByPath(String fileToCheck) {
        return manifest.entries().stream()
                .filter(entry -> entry.filePath().equals(fileToCheck))
                .findFirst()
                .orElse(null);
    }
}
