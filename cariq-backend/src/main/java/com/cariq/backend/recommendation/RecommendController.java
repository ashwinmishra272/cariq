package com.cariq.backend.recommendation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<RecommendResponse> recommend(@Valid @RequestBody RecommendRequest request) {
        return ResponseEntity.ok(recommendationService.recommend(request));
    }
}
