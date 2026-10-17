package io.github.alexbujenita.amberfic.core;

import java.util.List;

record ChecksumManifest(List<ChecksumEntry> entries, HashAlgorithm algorithm) {}
