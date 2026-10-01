package io.github.alexbujenita.amberfic.core.util;

import java.io.InputStream;

/**
 * Class used to fake Input Streams.
 * <br/>
 * Provides a stream of bytes without needing an existing large file.
 * <br/>
 * The overridden method returns the byte 0 without storing it
 * until it reaches the desired size, then -1.
 *
 */
public final class ZeroInputStream extends InputStream {

    private long bytesLeft;

    public ZeroInputStream(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be non negative. provided: " + size);
        }
        this.bytesLeft = size;
    }

    @Override
    public int read() {
        if (bytesLeft > 0) {
            bytesLeft--;
            return 0;
        }
        return -1;
    }
}
