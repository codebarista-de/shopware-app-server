package de.codebarista.shopware.appserver.config;

import de.codebarista.shopware.appserver.util.ByteArrayServletInputStream;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A custom implementation of {@code HttpServletRequestWrapper}. It provides a buffered
 * version of the request input stream, allowing repeated reads without consuming the original input stream.
 * The buffered content is stored in memory.
 */
class ReusableRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] requestBodyBytes;

    ReusableRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        try (InputStream inputStream = request.getInputStream()) {
            requestBodyBytes = inputStream.readAllBytes();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ByteArrayServletInputStream(requestBodyBytes);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(requestBodyBytes)));
    }

}
