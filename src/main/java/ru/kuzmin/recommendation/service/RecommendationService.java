package ru.kuzmin.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kuzmin.recommendation.model.dto.RecommendationRequest;
import ru.kuzmin.recommendation.model.dto.RecommendationResponse;
import ru.kuzmin.recommendation.model.entity.Item;
import ru.kuzmin.recommendation.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public RecommendationResponse getContentBasedRecommendations(RecommendationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. Получаем исходный товар
            Item sourceItem = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + request.getItemId()));

            // 2. Ищем похожие товары
            List<RecommendationResponse.RecommendedItem> similarItemsData;
            if (request.getCategoryFilter() != null) {
                similarItemsData = itemRepository.findSimilarItemsWithFilter(
                        request.getItemId(),
                        request.getCategoryFilter(),
                        request.getLimit()
                );
            } else {
                similarItemsData = itemRepository.findSimilarItems(
                        request.getItemId(),
                        request.getLimit()
                );
            }

            // 3. Преобразуем результаты в DTO
//            List<RecommendationResponse.RecommendedItem> recommendations =
//                    convertToRecommendationResponse(similarItemsData);

            // 4. Фильтрация по порогу сходства (если задан)
            if (request.getSimilarityThreshold() != null) {
                similarItemsData.removeIf(r -> r.getSimilarity() < request.getSimilarityThreshold());
            }

            // 5. Создаем ответ
            RecommendationResponse response = new RecommendationResponse();
            response.setSourceItemId(sourceItem.getId());
            response.setSourceItemTitle(sourceItem.getTitle());
            response.setRecommendations(similarItemsData);
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            log.info("Generated {} recommendations for item {} in {} ms",
                    similarItemsData.size(), request.getItemId(), response.getProcessingTimeMs());

            return response;

        } catch (Exception e) {
            log.error("Error generating recommendations for item {}", request.getItemId(), e);
            throw e;
        }
    }

//    private List<RecommendationResponse.RecommendedItem> convertToRecommendationResponse(
//            List<Object[]> data) {
//        List<RecommendationResponse.RecommendedItem> recommendations = new ArrayList<>();
//
//        for (Object[] row : data) {
//            RecommendationResponse.RecommendedItem item =
//                    new RecommendationResponse.RecommendedItem();
//
//            // Преобразование результата нативного запроса
//            // Индексы зависят от структуры SELECT запроса
//            //Item entity = (Item) row[0];
//            Double similarity = (Double) row[8];
//
//            item.setId((UUID) row[0]);
//            item.setTitle((String) row[1]);
//            item.setDescription((String) row[2]);
//            item.setCategory((String) row[3]);
//            item.setPrice( row[4] != null ?
//                    ((BigDecimal) row[4]).doubleValue() : null);
//            item.setSimilarity(similarity);
//
//            recommendations.add(item);
//        }
//
//        return recommendations;
//    }
}
