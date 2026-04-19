package com.cariq.backend.recommendation;

import com.cariq.backend.car.Car;
import com.cariq.backend.car.CarRepository;
import com.cariq.backend.session.Session;
import com.cariq.backend.session.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final String SYSTEM_PROMPT =
            "You are an expert Indian car advisor. Return only valid JSON, no markdown.";

    private static final String USER_PROMPT_TEMPLATE =
            "A buyer wants: budget=%s, use=%s, priorities=%s. " +
            "IMPORTANT — the buyer has the following hard constraints that must be strictly followed (do not recommend any car that violates these): %s. " +
            "Available cars (all within the buyer's budget — do NOT recommend cars outside this list): %s. " +
            "Pick the 3 best fits. Factor in userRating (out of 5) as a signal of real-world owner satisfaction — " +
            "prefer higher-rated cars when other attributes are comparable, but do not let it override a strong match on budget, use case, or priorities. " +
            "Return exactly: " +
            "{\"shortlist\": [\"<id1>\", \"<id2>\", \"<id3>\"], " +
            "\"reasoning\": {\"<id>\": \"why this fits in 1-2 sentences\"}, " +
            "\"tradeoff\": \"<paragraph>\"}";

    private final CarRepository carRepository;
    private final SessionRepository sessionRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public RecommendResponse recommend(RecommendRequest request) {
        double[] budgetRange = parseBudget(request.getBudget());
        double minPriceInr = budgetRange[0] * 100_000.0;
        double maxPriceInr = budgetRange[1] * 100_000.0;

        List<Car> filtered = carRepository.findAll().stream()
                .filter(car -> car.getPrice() >= minPriceInr && car.getPrice() <= maxPriceInr)
                .toList();

        Set<Long> filteredIds = filtered.stream()
                .map(Car::getId)
                .collect(Collectors.toSet());

        log.info("Budget='{}' → [{}, {}] INR → {} matching cars",
                request.getBudget(), minPriceInr, maxPriceInr, filtered.size());

        String budgetDesc = formatBudgetDescription(budgetRange);

        try {
            String carsJson = objectMapper.writeValueAsString(filtered);
            String userPrompt = USER_PROMPT_TEMPLATE.formatted(
                    budgetDesc, request.getUse(), request.getPriorities(), request.getExtra(), carsJson);

            String content = openAiClient.complete(SYSTEM_PROMPT, userPrompt);

            JsonNode parsed = objectMapper.readTree(content);

            List<String> shortlistIds = new ArrayList<>();
            parsed.get("shortlist").forEach(node -> shortlistIds.add(node.asText()));

            Map<String, String> reasoning = new LinkedHashMap<>();
            parsed.get("reasoning").fields()
                    .forEachRemaining(e -> reasoning.put(e.getKey(), e.getValue().asText()));

            String tradeoff = parsed.get("tradeoff").asText();

            List<Car> shortlistedCars = shortlistIds.stream()
                    .map(Long::parseLong)
                    .filter(id -> {
                        if (!filteredIds.contains(id)) {
                            log.warn("GPT returned car id={} which is outside the budget-filtered set — discarding", id);
                            return false;
                        }
                        return true;
                    })
                    .map(id -> carRepository.findById(id))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            Session session = new Session();
            session.setAnswersJson(objectMapper.writeValueAsString(request));
            session.setRecommendationsJson(content);
            Session saved = sessionRepository.save(session);

            log.info("Recommendation session '{}' saved with {} shortlisted cars",
                    saved.getId(), shortlistedCars.size());

            return new RecommendResponse(saved.getId(), shortlistedCars, reasoning, tradeoff);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process recommendation data", e);
        }
    }

    /**
     * Extracts min/max budget in lakhs from a free-text string.
     * Examples: "5-10" → [5.0, 10.0], "under 12L" → [0.0, 12.0], "20 lakhs" → [0.0, 20.0]
     */
    private double[] parseBudget(String budget) {
        Matcher matcher = Pattern.compile("\\d+(\\.\\d+)?").matcher(budget);
        List<Double> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        if (numbers.size() >= 2) return new double[]{numbers.get(0), numbers.get(1)};
        if (numbers.size() == 1) return new double[]{0.0, numbers.get(0)};
        return new double[]{0.0, 9999.0};
    }

    private String formatBudgetDescription(double[] budgetRange) {
        if (budgetRange[0] == 0) {
            return "under ₹%.0f lakh".formatted(budgetRange[1]);
        } else if (budgetRange[1] >= 9000) {
            return "above ₹%.0f lakh".formatted(budgetRange[0]);
        } else {
            return "₹%.0f–₹%.0f lakh".formatted(budgetRange[0], budgetRange[1]);
        }
    }
}
