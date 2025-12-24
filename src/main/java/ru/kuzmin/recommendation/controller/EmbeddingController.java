package ru.kuzmin.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kuzmin.recommendation.ai.service.OllamaEmbeddingService;
import ru.kuzmin.recommendation.repository.ItemRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/embeddings")
@RequiredArgsConstructor
@Tag(name = "Embedding Management", description = "Управление эмбеддингами товаров")
public class EmbeddingController {

    private final OllamaEmbeddingService embeddingService;
    private final ItemRepository itemRepository;

    @PostMapping("/generate/{itemId}")
    @Operation(summary = "Сгенерировать эмбеддинг для конкретного товара")
    public ResponseEntity<Map<String, Object>> generateEmbedding(@PathVariable UUID itemId) {
        Map<String, Object> response = new HashMap<>(); //TODO сделать модели

        try {
            var item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));

            embeddingService.generateAndSaveEmbedding(item);

            response.put("success", true);
            response.put("message", "Embedding generated for item: " + itemId);
            response.put("itemId", itemId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/batch-generate")
    @Operation(summary = "Запустить пакетную генерацию эмбеддингов")
    public ResponseEntity<Map<String, Object>> batchGenerateEmbeddings(
            @RequestParam(defaultValue = "50") int batchSize) {

        Map<String, Object> response = new HashMap<>(); //TODO сделать модели

        long itemsWithoutEmbeddings = itemRepository.countItemsWithoutEmbeddings();

        if (itemsWithoutEmbeddings == 0) {
            response.put("message", "All items already have embeddings");
            response.put("itemsWithoutEmbeddings", 0);
            return ResponseEntity.ok(response);
        }

        // Запускаем асинхронно
        embeddingService.batchGenerateEmbeddings(batchSize);

        response.put("success", true);
        response.put("message", "Batch embedding generation started");
        response.put("itemsToProcess", itemsWithoutEmbeddings);
        response.put("batchSize", batchSize);

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/refresh-all")
    @Operation(summary = "Перегенерировать все эмбеддинги")
    public ResponseEntity<Map<String, Object>> refreshAllEmbeddings() {
        Map<String, Object> response = new HashMap<>(); //TODO сделать модели

        long totalItems = itemRepository.count();

        // Запускаем асинхронно
        embeddingService.refreshAllEmbeddings();

        response.put("success", true);
        response.put("message", "Full embedding refresh started");
        response.put("totalItems", totalItems);
        response.put("warning", "This operation may take significant time");

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Статистика по эмбеддингам")
    public ResponseEntity<Map<String, Object>> getEmbeddingStats() {
        long totalItems = itemRepository.count();
        long itemsWithoutEmbeddings = itemRepository.countItemsWithoutEmbeddings();
        long itemsWithEmbeddings = totalItems - itemsWithoutEmbeddings;

        Map<String, Object> stats = new HashMap<>(); //TODO сделать модели
        stats.put("totalItems", totalItems);
        stats.put("itemsWithEmbeddings", itemsWithEmbeddings);
        stats.put("itemsWithoutEmbeddings", itemsWithoutEmbeddings);
        stats.put("coveragePercentage",
                totalItems > 0 ? (itemsWithEmbeddings * 100.0 / totalItems) : 0);

        return ResponseEntity.ok(stats);
    }
}
