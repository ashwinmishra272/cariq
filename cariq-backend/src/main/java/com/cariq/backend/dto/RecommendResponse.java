package com.cariq.backend.dto;

import com.cariq.backend.model.Car;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RecommendResponse {
    private String sessionId;
    private List<Car> shortlist;
    private Map<String, String> reasoning;
    private String tradeoff;
}