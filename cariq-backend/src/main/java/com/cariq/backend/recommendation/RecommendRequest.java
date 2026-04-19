package com.cariq.backend.recommendation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RecommendRequest {

    @NotBlank(message = "Budget is required")
    private String budget;

    @NotBlank(message = "Use case is required")
    private String use;

    private List<String> priorities;
    private String extra;
}
