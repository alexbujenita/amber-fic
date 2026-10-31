package io.github.alexbujenita.amberfic.core;

/**
 * A listener that gets called with a new entry in the integrity checker
 * every readable scanned file.
 */
@FunctionalInterface
interface ProgressListener {
    void onFileChecked(ProgressTracking progressTracking);
}
