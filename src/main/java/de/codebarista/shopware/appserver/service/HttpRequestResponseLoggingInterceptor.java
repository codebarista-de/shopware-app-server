package de.codebarista.shopware.appserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpRequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger("AdminApiHTTPClient");

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        logger.atDebug()
                .setMessage("Request")
                .addKeyValue("URI: {}", request.getURI())
                .addKeyValue("Method: {}", request.getMethod())
                .addKeyValue("Headers: {}", request.getHeaders())
                .addKeyValue("Body: {}", new String(body, StandardCharsets.UTF_8))
                .log();
    }

    private void logResponse(ClientHttpResponse response) throws java.io.IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line).append('\n');
        }
        logger.atDebug()
                .setMessage("Response")
                .addKeyValue("Status Code: {}", response.getStatusCode())
                .addKeyValue("Headers: {}", response.getHeaders())
                .addKeyValue("Body: {}", body)
                .log();
    }
}
