package io.github.alexbujenita.amberfic.core.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generates zero-valued bytes to test hashing when reads are short.
 *
 * <p>Each bulk-read call with a positive requested length delivers only one
 * byte while data remains, even when the buffer has room for more.
 * This lets tests check that the hasher continues reading until end of stream.
 */
public final class ShortReadInputStream extends InputStream {

    private long bytesLeft;

    public ShortReadInputStream(long size) {
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

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (length == 0) return 0;
        int nextByte = read();
        if (nextByte == -1) {
            return -1;
        }
        buffer[offset] = (byte) nextByte;
        return 1;
    }
}
