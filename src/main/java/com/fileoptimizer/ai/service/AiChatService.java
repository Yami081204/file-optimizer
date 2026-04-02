package com.fileoptimizer.ai.service;

import com.fileoptimizer.ai.client.AiClient;
import com.fileoptimizer.ai.model.AiResponse;
import com.fileoptimizer.common.exception.AiException;


public class AiChatService {

    private final AiClient aiClient = new AiClient();
    private static final String MODEL = "gpt-3.5-turbo";

    public AiResponse ask(String userPrompt) {
        String systemPrompt = "You are a helpful File Optimizer Assistant. " +
                "Help the user manage, clean, and organize their files. " +
                "Keep responses concise and technical.";

        try {
            String rawJson = aiClient.sendRequest(MODEL, systemPrompt, userPrompt);
            String content = parseResponseContent(rawJson);
            return new AiResponse(content);
        } catch (AiException e) {
            return new AiResponse("Error: " + e.getMessage());
        }
    }

    private String parseResponseContent(String json) {
        try {
            int start = json.indexOf("\"content\": \"") + 12;
            int end = json.indexOf("\"", start);
            return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception e) {
            return "Could not parse AI response: " + e.getMessage();
        }
    }
}
