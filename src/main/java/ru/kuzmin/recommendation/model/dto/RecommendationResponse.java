package ru.kuzmin.recommendation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class RecommendationResponse {
    private UUID sourceItemId;
    private String sourceItemTitle;
    private List<RecommendedItem> recommendations;
    private Long processingTimeMs;

    @Data
    @AllArgsConstructor
    public static class RecommendedItem {
        private UUID id;
        private String title;
        private String description;
        private String category;
        private BigDecimal price;
        private Double similarity; // 0-1, где 1 - идентичные
    }
}
