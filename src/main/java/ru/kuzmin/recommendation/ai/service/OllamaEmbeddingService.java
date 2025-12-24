package ru.kuzmin.recommendation.ai.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.kuzmin.recommendation.model.entity.Item;
import ru.kuzmin.recommendation.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final ItemRepository itemRepository;

    /**
     * Генерация эмбеддинга для текста
     */
    public float[] generateEmbedding(String text) {
        log.debug("Generating embedding for text: {}", text.substring(0, Math.min(50, text.length())));

        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));

        return response.getResult().getOutput();
    }

    /**
     * Генерация контента для эмбеддинга из товара
     */
    public String prepareTextForEmbedding(Item item) {
        StringBuilder sb = new StringBuilder();

        if (item.getTitle() != null) {
            sb.append(item.getTitle()).append(". ");
        }

        if (item.getDescription() != null) {
            sb.append(item.getDescription()).append(". ");
        }

        if (item.getCategory() != null) {
            sb.append("Category: ").append(item.getCategory()).append(". ");
        }

        // Можно добавить другие поля
        if (sb.length() > 1000) {
            return sb.substring(0, 1000);
        }

        return sb.toString();
    }

    /**
     * Генерация и сохранение эмбеддинга для одного товара
     */
    @Transactional
    public void generateAndSaveEmbedding(Item item) {
        try {
            String text = prepareTextForEmbedding(item);
            if (text.trim().isEmpty()) {
                log.warn("No text content for item {}", item.getId());
                return;
            }

            float[] embedding = generateEmbedding(text);
            item.setEmbedding(embedding);
            itemRepository.save(item);

            log.info("Generated embedding for item {}: {}", item.getId(), item.getTitle());

        } catch (Exception e) {
            log.error("Failed to generate embedding for item {}", item.getId(), e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    /**
     * Пакетная обработка товаров без эмбеддингов
     */
    @Async("embeddingTaskExecutor")
    public void batchGenerateEmbeddings(int batchSize) {
        log.info("Starting batch embedding generation...");

        int processed = 0;
        int page = 0;

        while (true) {
            List<Item> items = itemRepository.findItemsWithoutEmbedding(
                    page * batchSize,
                    batchSize
            );

            if (items.isEmpty()) {
                break;
            }

            for (Item item : items) {
                generateAndSaveEmbedding(item);
                processed++;

                // Небольшая пауза между запросами
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            page++;
            log.info("Processed {} items...", processed);
        }

        log.info("Batch embedding generation completed. Total processed: {}", processed);
    }

    /**
     * Обновление эмбеддингов для всех товаров
     */
    @Async
    public void refreshAllEmbeddings() {
        log.warn("Starting refresh of ALL embeddings. This may take a while...");

        List<Item> allItems = itemRepository.findAll();
        int total = allItems.size();
        int processed = 0;

        for (Item item : allItems) {
            generateAndSaveEmbedding(item);
            processed++;

            if (processed % 100 == 0) {
                log.info("Refresh progress: {}/{} ({}%)",
                        processed, total, (processed * 100 / total));
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("All embeddings refreshed. Total items: {}", total);
    }
}