package com.fileoptimizer.ai.client;

import com.fileoptimizer.common.exception.AiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class AiClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final HttpClient httpClient;

    public AiClient() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new AiException("OpenAI API Key not found in environment variables (OPENAI_API_KEY).");
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String sendRequest(String model, String systemPrompt, String userPrompt) {
        String jsonPayload = String.format(
            "{\"model\": \"%s\", \"messages\": [" +
            "{\"role\": \"system\", \"content\": \"%s\"}," +
            "{\"role\": \"user\", \"content\": \"%s\"}" +
            "]}", model, escapeJson(systemPrompt), escapeJson(userPrompt)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(30))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AiException("OpenAI API returned error code: " + response.statusCode() + " - " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("Failed to communicate with OpenAI API.", e);
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
