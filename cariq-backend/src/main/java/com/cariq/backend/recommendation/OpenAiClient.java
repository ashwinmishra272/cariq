package com.cariq.backend.recommendation;

import com.cariq.backend.config.OpenAiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    /**
     * Sends a chat completion request to OpenAI and returns the assistant's response content.
     * Markdown code fences are stripped automatically if present.
     */
    public String complete(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", openAiProperties.model());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", openAiProperties.temperature());

        try {
            String requestJson = objectMapper.writeValueAsString(body);

            log.debug("Sending OpenAI request with model={}", openAiProperties.model());

            String responseBody = openAiRestClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.at("/choices/0/message/content").asText();

            if (content.trim().startsWith("```")) {
                content = content.trim()
                        .replaceAll("^```[a-zA-Z]*\\n?", "")
                        .replaceAll("```$", "")
                        .trim();
            }

            log.debug("OpenAI response received, content length={}", content.length());
            return content;

        } catch (RestClientException e) {
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process OpenAI request/response", e);
        }
    }
}
