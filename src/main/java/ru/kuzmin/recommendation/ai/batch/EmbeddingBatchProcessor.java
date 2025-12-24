package ru.kuzmin.recommendation.ai.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kuzmin.recommendation.ai.service.OllamaEmbeddingService;
import ru.kuzmin.recommendation.repository.ItemRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class EmbeddingBatchProcessor implements CommandLineRunner {

    private final OllamaEmbeddingService embeddingService;
    private final ItemRepository itemRepository;

    // Размер батча для обработки
    private static final int BATCH_SIZE = 50;

    /**
     * Автоматически генерируем эмбеддинги при старте приложения
     * для товаров, у которых их нет
     */
    @Override
    public void run(String... args) {
        log.info("Checking for items without embeddings...");

        long itemsWithoutEmbeddings = itemRepository.countItemsWithoutEmbeddings();

        if (itemsWithoutEmbeddings > 0) {
            log.info("Found {} items without embeddings. Starting batch generation...",
                    itemsWithoutEmbeddings);

            // Запускаем в отдельном потоке, чтобы не блокировать старт приложения
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Даем приложению полностью запуститься
                    embeddingService.batchGenerateEmbeddings(BATCH_SIZE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            log.info("All items have embeddings.");
        }
    }

    /**
     * Ежедневное обновление эмбеддингов (например, в 3 ночи)
     */
    @Scheduled(cron = "0 0 3 * * ?") // Каждый день в 3:00
    public void scheduledEmbeddingUpdate() {
        log.info("Starting scheduled embedding update...");

        // Можно реализовать инкрементальное обновление:
        // 1. Товары, измененные за последние 24 часа
        // 2. Товары с низким качеством рекомендаций

        embeddingService.batchGenerateEmbeddings(BATCH_SIZE);
    }

    /**
     * Еженедельное полное обновление
     */
    @Scheduled(cron = "0 0 4 * * SUN") // Каждое воскресенье в 4:00
    public void fullEmbeddingRefresh() {
        log.info("Starting weekly full embedding refresh...");
        embeddingService.refreshAllEmbeddings();
    }
}

