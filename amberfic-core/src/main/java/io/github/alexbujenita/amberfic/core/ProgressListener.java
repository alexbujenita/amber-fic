package io.github.alexbujenita.amberfic.core;

/**
 * Notified by the integrity checker after it finishes each scanned file, including files that could not be read. Each call receives that file's result and the running totals of files and
 * bytes. Manifest entries missing from the scan are not reported. An exception thrown by the listener aborts the run.
 */
@FunctionalInterface
interface ProgressListener {
    void onFileChecked(ProgressTracking progressTracking);
}
