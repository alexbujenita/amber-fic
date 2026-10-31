package io.github.alexbujenita.amberfic.core;

record ProgressTracking(IntegrityCheckResult checkResult, long filesDone, long cumulativeBytes) {}
