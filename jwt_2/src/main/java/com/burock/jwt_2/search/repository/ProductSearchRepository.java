package com.burock.jwt_2.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.burock.jwt_2.search.model.ProductIndex;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductIndex, String> {

    // Basit text arama
    @Query("""
            {
                "bool": {
                    "should": [
                        { "match_phrase_prefix": {"name": "?0"}},
                        { "match_phrase_prefix": {"categoryName": "?0"} }
                    ]
                }
            }
            """)
    Page<ProductIndex> searchByText(String q, Pageable pageable);

    Page<ProductIndex> findByCategoryId(String categoryId, Pageable pageable);

    @Query("""
            {
                "range": {
                    "price": {
                        "gte": ?0,
                        "lte": ?1
                    }
                }
            }
            """) // gte: >= , lte: <=

    Page<ProductIndex> findByPriceBetween(double minPrice, double maxPrice, Pageable pageable);

    @Query("""
            {
                "range": {
                    "stock": {
                        "gt": 0
                    }
                }
            }
            """) // gt:Greater than(büyük)
    Page<ProductIndex> findInStock(Pageable pageable);
}
