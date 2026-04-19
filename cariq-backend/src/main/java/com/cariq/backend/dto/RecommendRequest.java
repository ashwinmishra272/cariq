package com.cariq.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendRequest {
    private String budget;
    private String use;
    private List<String> priorities;
    private String extra;
}