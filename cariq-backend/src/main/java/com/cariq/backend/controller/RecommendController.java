package com.cariq.backend.controller;

import com.cariq.backend.dto.RecommendRequest;
import com.cariq.backend.dto.RecommendResponse;
import com.cariq.backend.model.Car;
import com.cariq.backend.model.Session;
import com.cariq.backend.repository.CarRepository;
import com.cariq.backend.repository.SessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RecommendController {

    private final CarRepository carRepository;
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostMapping
    public ResponseEntity<?> recommend(@RequestBody RecommendRequest request) {

        // 1. Parse budget string into min/max lakhs, filter by maxPrice
        double[] budgetRange = parseBudget(request.getBudget());
        double maxPriceInr = budgetRange[1] * 100000.0;

        List<Car> filtered = carRepository.findAll().stream()
                .filter(car -> car.getPrice() <= maxPriceInr)
                .toList();

        // 2. Build prompt
        try {
            String carsJson = objectMapper.writeValueAsString(filtered);

            String systemPrompt = "You are an expert Indian car advisor. Return only valid JSON, no markdown.";

            String userPrompt = String.format(
                    "A buyer wants: budget=%s, use=%s, priorities=%s, extra=%s. " +
                    "Available cars: %s. " +
                    "Return exactly: {\"shortlist\": [\"<id1>\", \"<id2>\", \"<id3>\"], " +
                    "\"reasoning\": {\"<id>\": \"why this fits in 1-2 sentences\"}, " +
                    "\"tradeoff\": \"<paragraph>\"}",
                    request.getBudget(), request.getUse(),
                    request.getPriorities(), request.getExtra(),
                    carsJson
            );

            // 3. Serialize OpenAI request body via ObjectMapper
            Map<String, Object> openaiBody = new LinkedHashMap<>();
            openaiBody.put("model", "gpt-4o");
            openaiBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user",   "content", userPrompt)
            ));
            openaiBody.put("temperature", 0.7);

            String openaiBodyJson = objectMapper.writeValueAsString(openaiBody);

            // 4. Call OpenAI via HttpClient
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(openaiBodyJson))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 5. Extract choices[0].message.content via ObjectMapper
            JsonNode openaiRoot = objectMapper.readTree(httpResponse.body());
            String content = openaiRoot.at("/choices/0/message/content").asText();

            // 6. Parse the returned JSON via ObjectMapper
            JsonNode parsed = objectMapper.readTree(content);

            List<String> shortlistIds = new ArrayList<>();
            parsed.get("shortlist").forEach(node -> shortlistIds.add(node.asText()));

            Map<String, String> reasoning = new LinkedHashMap<>();
            parsed.get("reasoning").fields()
                    .forEachRemaining(e -> reasoning.put(e.getKey(), e.getValue().asText()));

            String tradeoff = parsed.get("tradeoff").asText();

            // 7. Fetch full Car objects for shortlisted ids
            List<Car> shortlistedCars = shortlistIds.stream()
                    .map(id -> carRepository.findById(Long.parseLong(id)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            // 8. Save session to DB
            Session session = new Session();
            session.setAnswersJson(objectMapper.writeValueAsString(request));
            session.setRecommendationsJson(content);
            Session saved = sessionRepository.save(session);

            // 9. Return response
            return ResponseEntity.ok(new RecommendResponse(saved.getId(), shortlistedCars, reasoning, tradeoff));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Recommendation failed: " + e.getMessage());
        }
    }

    /**
     * Extracts min and max budget values (in lakhs) from a free-text string.
     * "10-15 lakhs" → [10.0, 15.0]
     * "under 12 lakhs" → [0.0, 12.0]
     * "20 lakhs"       → [0.0, 20.0]
     */
    private double[] parseBudget(String budget) {
        Matcher matcher = Pattern.compile("\\d+(\\.\\d+)?").matcher(budget);
        List<Double> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        if (numbers.size() >= 2) return new double[]{numbers.get(0), numbers.get(1)};
        if (numbers.size() == 1) return new double[]{0.0, numbers.get(0)};
        return new double[]{0.0, Double.MAX_VALUE / 100000.0};
    }
}