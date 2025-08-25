package com.burock.jwt_2.search.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.burock.jwt_2.search.model.CartIndex;

public interface CartSearchRepository extends ElasticsearchRepository<CartIndex, String> {

    Optional<CartIndex> findByUserId(String userId);

    @Query("""
            {
                "bool": {
                    "should": [
                        { "match_phrase_prefix": {"userName": "?0"}},
                        { "fuzzy": {"userName": {"value": "?0", "fuzziness": "AUTO"}}}
                    ]
                }
            }
            """)
    Page<CartIndex> searchByUserName(String userName, Pageable pageable);

    @Query("""
            {
                "nested": {
                    "path": "items",
                    "query": {
                        "term": {"items.productId": "?0"}
                    }
                }
            }
            """)
    Page<CartIndex> findByProductId(String productId, Pageable pageable);

    @Query("""
            {
                "range": {
                    "totalPrice": {
                        "gte": ?0,
                        "lte": ?1
                    }
                }
            }
            """)
    Page<CartIndex> findByTotalPriceBetween(double minPrice, double maxPrice, Pageable pageable);
}
