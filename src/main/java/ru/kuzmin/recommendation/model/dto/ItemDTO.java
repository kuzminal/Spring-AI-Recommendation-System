package ru.kuzmin.recommendation.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemDTO {
    private Long id;
    private String externalId;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
}
