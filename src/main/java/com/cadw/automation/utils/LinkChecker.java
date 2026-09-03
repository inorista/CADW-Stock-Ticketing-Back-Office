package com.cadw.automation.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class LinkChecker {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private LinkChecker() {
    }

    public static List<LinkStatus> check(Collection<URI> links) {
        return links.parallelStream()
                .map(LinkChecker::checkOne)
                .sorted(Comparator.comparing(status -> status.uri().toString()))
                .toList();
    }

    public static boolean isBroken(LinkStatus status) {
        return status.statusCode() < 200 || status.statusCode() >= 400;
    }

    private static LinkStatus checkOne(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Mozilla/5.0 SeleniumLinkChecker/1.0")
                .header("Accept", "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
                .GET()
                .build();
        try {
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            return new LinkStatus(uri, response.statusCode(), "");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new LinkStatus(uri, -1, "Interrupted while checking link");
        } catch (IOException | IllegalArgumentException exception) {
            return new LinkStatus(uri, -1, exception.getMessage());
        }
    }

    public record LinkStatus(URI uri, int statusCode, String error) {
        @Override
        public String toString() {
            String details = error == null || error.isBlank() ? "" : " (" + error + ")";
            return statusCode + " " + uri + details;
        }
    }
}
