package com.dtf.reading_tracker_server.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient openLibraryRestClient(
            @Value("${open-library.base-url:https://openlibrary.org}") String baseUrl,
            @Value("${open-library.connect-timeout:2s}") Duration connectTimeout,
            @Value("${open-library.read-timeout:5s}") Duration readTimeout,
            @Value("${open-library.max-attempts:3}") int maxAttempts,
            @Value("${open-library.retry-backoff:200ms}") Duration retryBackoff
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        var requestFactory = new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "reading-tracker-server")
                .requestInterceptor((request, body, execution) -> {
                    int attempts = Math.max(1, maxAttempts);
                    for (int attempt = 1; ; attempt++) {
                        try {
                            var response = execution.execute(request, body);
                            int status = response.getStatusCode().value();
                            if (attempt >= attempts || (status != 429 && status < 500)) {
                                return response;
                            }
                            response.close();
                        } catch (IOException ex) {
                            if (attempt >= attempts) {
                                throw ex;
                            }
                        }
                        sleepBeforeRetry(retryBackoff, attempt);
                    }
                })
                .build();
    }

    private static void sleepBeforeRetry(Duration baseDelay, int attempt) throws IOException {
        long delayMillis = Math.max(0, baseDelay.toMillis()) * (1L << Math.min(attempt - 1, 10));
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while retrying outbound request", ex);
        }
    }
}
