package io.github.alexbujenita.amberfic.core;

sealed interface IntegrityCheckResult {
    record Passed(String filePath) implements IntegrityCheckResult {}

    record Failed(String filePath) implements IntegrityCheckResult {}

    record MissingFromManifest(String filePath) implements IntegrityCheckResult {}

    record MissingFromScan(String filePath) implements IntegrityCheckResult {}
}
