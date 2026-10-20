package io.github.alexbujenita.amberfic.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class IntegrityChecker {
    private final List<IntegrityCheckResult> integrityResults;
    private final List<ScannedFile> scannedFiles;
    private final ChecksumManifest manifest;
    private final Hasher hasher;

    IntegrityChecker(List<ScannedFile> scannedFiles, ChecksumManifest manifest) {
        this.scannedFiles = scannedFiles;
        this.manifest = manifest;
        this.integrityResults = new ArrayList<>();
        this.hasher = new Hasher(manifest.algorithm());
    }

    List<IntegrityCheckResult> calculateIntegrityResults() throws IOException {
        for (ScannedFile fileToCheck : scannedFiles) {

            try (InputStream inputStream = Files.newInputStream(fileToCheck.path())) {
                final ChecksumEntry entryToCheck =
                        new ChecksumEntry(hasher.hash(inputStream), fileToCheck.relativeName());
                final ChecksumEntry manifestEntry = getManifestEntryByPath(fileToCheck.relativeName());
                addIntegrityResult(entryToCheck, manifestEntry);
            }
        }

        List<IntegrityCheckResult> results = List.copyOf(this.integrityResults);
        this.integrityResults.clear();
        return results;
    }

    private void addIntegrityResult(ChecksumEntry entryToCheck, ChecksumEntry manifestEntry) {
        if (manifestEntry == null) {
            integrityResults.add(new IntegrityCheckResult.MissingFromManifest(entryToCheck.filePath()));
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
