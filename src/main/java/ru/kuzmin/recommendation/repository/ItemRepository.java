package ru.kuzmin.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kuzmin.recommendation.model.dto.RecommendationResponse;
import ru.kuzmin.recommendation.model.entity.Item;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    // Поиск по сходству с использованием Spring AI VectorStore
    @Query(value = """
            WITH source_embedding AS (
                SELECT embedding FROM items WHERE id = :itemId
            )
            SELECT i.*, 
                   1 - (i.embedding <=> (SELECT embedding FROM source_embedding)) AS similarity
            FROM items i
            WHERE i.id != :itemId
              AND i.embedding IS NOT NULL
              AND (:minSimilarity IS NULL OR 
                   1 - (i.embedding <=> (SELECT embedding FROM source_embedding)) >= :minSimilarity)
            ORDER BY i.embedding <=> (SELECT embedding FROM source_embedding)
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findSimilarItemsWithThreshold(
            @Param("itemId") UUID itemId,
            @Param("limit") int limit,
            @Param("minSimilarity") Double minSimilarity);

    // Поиск по косинусному сходству (основной метод)
    @Query(value = """
            SELECT 
                    i.id,
                    i.title,
                    i.description,
                    i.category,
                    i.price,
                    1 - (i.embedding <=> (SELECT embedding FROM items WHERE id = :itemId)) AS similarity
            FROM items i
            WHERE i.id != :itemId 
            AND i.embedding IS NOT NULL
            ORDER BY i.embedding <=> (SELECT embedding FROM items WHERE id = :itemId)
            LIMIT :limit
            """, nativeQuery = true)
    List<RecommendationResponse.RecommendedItem> findSimilarItems(@Param("itemId") UUID itemId,
                                                                  @Param("limit") int limit);

    // Поиск с фильтрацией по категории
    @Query(value = """
            SELECT 
                    i.id,
                    i.title,
                    i.description,
                    i.category,
                    i.price,
                    1 - (i.embedding <=> (SELECT embedding FROM items WHERE id = :itemId)) AS similarity
            FROM items i
            WHERE i.id != :itemId 
            AND i.embedding IS NOT NULL
            AND (:category IS NULL OR i.category = :category)
            ORDER BY i.embedding <=> (SELECT embedding FROM items WHERE id = :itemId)
            LIMIT :limit
            """, nativeQuery = true)
    List<RecommendationResponse.RecommendedItem> findSimilarItemsWithFilter(@Param("itemId") UUID itemId,
                                                                            @Param("category") String category,
                                                                            @Param("limit") int limit);

    // Находим товары без эмбеддингов
    @Query(value = """
            SELECT * FROM items 
            WHERE embedding IS NULL 
            ORDER BY id 
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Item> findItemsWithoutEmbedding(@Param("offset") int offset,
                                         @Param("limit") int limit);

    // Получаем количество товаров без эмбеддингов
    @Query("SELECT COUNT(i) FROM Item i WHERE i.embedding IS NULL")
    long countItemsWithoutEmbeddings();
}