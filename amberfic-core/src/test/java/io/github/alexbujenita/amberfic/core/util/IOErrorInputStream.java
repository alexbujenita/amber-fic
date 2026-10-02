package io.github.alexbujenita.amberfic.core.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class used to fake Input Streams.
 * <br/>
 * Fails when needed for testing.
 *
 */
public final class IOErrorInputStream extends InputStream {

    private long bytesLeft;
    private int bytesDelivered;
    private final int failAfter;

    public IOErrorInputStream(long size, int failAfter) {
        this.failAfter = failAfter;
        this.bytesDelivered = 0;
        if (size < 0) {
            throw new IllegalArgumentException("size must be non negative. provided: " + size);
        }
        this.bytesLeft = size;
    }

    @Override
    public int read() throws IOException {
        if (bytesLeft > 0) {
            if (failAfter == bytesDelivered) throw new IOException("Simulated read failure");
            bytesLeft--;
            bytesDelivered++;
            return 0;
        }
        return -1;
    }
}
