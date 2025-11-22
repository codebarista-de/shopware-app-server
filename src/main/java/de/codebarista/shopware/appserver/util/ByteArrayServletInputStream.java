package de.codebarista.shopware.appserver.util;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A custom implementation of {@code ServletInputStream} that reads data
 * from a byte array. This input stream is suitable for scenarios where
 * you want to provide servlet input from an in-memory byte array.
 */
public class ByteArrayServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream byteArrayInputStream;

    public ByteArrayServletInputStream(byte[] data) {
        this.byteArrayInputStream = new ByteArrayInputStream(data);
    }

    @Override
    public int read() {
        return byteArrayInputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        return byteArrayInputStream.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new UnsupportedOperationException("Switching to non-blocking IO is not supported.");
    }

    @Override
    public void close() throws IOException {
        byteArrayInputStream.close();
    }
}
