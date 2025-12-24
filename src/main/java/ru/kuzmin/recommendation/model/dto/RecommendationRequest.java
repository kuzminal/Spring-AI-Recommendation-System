package ru.kuzmin.recommendation.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
public class RecommendationRequest {
    @NotNull
    private UUID itemId;

    private String userId; // Для сбора метрик

    private String categoryFilter;

    @NotNull
    private Integer limit = 10;

    private Double similarityThreshold; // Минимальное сходство (опционально)
}
