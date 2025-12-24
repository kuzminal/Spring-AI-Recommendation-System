package ru.kuzmin.recommendation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kuzmin.recommendation.model.dto.RecommendationRequest;
import ru.kuzmin.recommendation.model.dto.RecommendationResponse;
import ru.kuzmin.recommendation.service.RecommendationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/content-based")
    public ResponseEntity<RecommendationResponse> getContentBasedRecommendations(
            @Valid @RequestBody RecommendationRequest request) {

        RecommendationResponse response =
                recommendationService.getContentBasedRecommendations(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/content-based/{itemId}")
    public ResponseEntity<RecommendationResponse> getContentBasedRecommendations(
            @PathVariable UUID itemId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") Integer limit) {

        RecommendationRequest request = new RecommendationRequest();
        request.setItemId(itemId);
        request.setUserId(userId);
        request.setCategoryFilter(category);
        request.setLimit(limit);

        RecommendationResponse response =
                recommendationService.getContentBasedRecommendations(request);

        return ResponseEntity.ok(response);
    }
}
