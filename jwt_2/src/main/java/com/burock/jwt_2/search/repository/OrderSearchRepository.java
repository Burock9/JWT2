package com.burock.jwt_2.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.burock.jwt_2.search.model.OrderIndex;

public interface OrderSearchRepository extends ElasticsearchRepository<OrderIndex, String> {

    @Query("""
            {
                "bool": {
                    "should": [
                        { "match_phrase_prefix": {"orderNumber": "?0"}},
                        { "match_phrase_prefix": {"username": "?0"}},
                        { "match_phrase_prefix": {"shippingAddress": "?0"}},
                        { "match": {"notes": "?0"}}
                    ]
                }
            }
            """)
    Page<OrderIndex> searchByText(String query, Pageable pageable);

    Page<OrderIndex> findByStatus(String status, Pageable pageable);

    Page<OrderIndex> findByUsername(String username, Pageable pageable);

    Page<OrderIndex> findByUserId(String userId, Pageable pageable);

    @Query("""
            {
                "range": {
                    "totalAmount": {
                        "gte": ?0,
                        "lte": ?1
                    }
                }
            }
            """)
    Page<OrderIndex> findByTotalAmountBetween(double minAmount, double maxAmount, Pageable pageable);
}
