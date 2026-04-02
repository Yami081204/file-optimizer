package com.fileoptimizer.app.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AiChatService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3"; // hoặc mistral, gemma...

    public String askAI(String userMessage) {
        try {
            URL url = new URL(OLLAMA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String prompt = buildPrompt(userMessage);

            String requestBody = """
            {
              "model": "%s",
              "prompt": "%s",
              "stream": false
            }
            """.formatted(MODEL, escapeJson(prompt));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes("utf-8"));
            }

            int status = conn.getResponseCode();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            status >= 200 && status < 300
                                    ? conn.getInputStream()
                                    : conn.getErrorStream(),
                            "utf-8"
                    )
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }

            if (status != 200) {
                return "❌ Lỗi Ollama API: " + response;
            }

            return extractResponse(response.toString());

        } catch (Exception e) {
            return "⚠️ Không kết nối được Ollama.\nHãy đảm bảo:\n- Ollama đã chạy\n- Model đã pull (ollama run llama3)\n\nChi tiết: " + e.getMessage();
        }
    }

    // Prompt định hướng AI theo app của bạn
    private String buildPrompt(String userMessage) {
        return """
        Bạn là trợ lý AI trong ứng dụng quản lý file.
        Trả lời tiếng Việt rõ ràng, ngắn gọn, hữu ích.

        Ngữ cảnh:
        - Ứng dụng có chức năng: quét file, dọn rác, tìm file trùng, phân tích hệ thống.

        Người dùng hỏi:
        %s
        """.formatted(userMessage);
    }

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String extractResponse(String json) {
        try {
            int start = json.indexOf("\"response\":\"");
            if (start == -1) return json;

            start += 12;
            int end = json.indexOf("\"", start);

            String content = json.substring(start, end);

            return content
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

        } catch (Exception e) {
            return json;
        }
    }
}