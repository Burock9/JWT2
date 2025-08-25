package com.burock.jwt_2.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.burock.jwt_2.search.model.CategoryIndex;

public interface CategorySearchRepository extends ElasticsearchRepository<CategoryIndex, String> {

    @Query("""
            {
                "bool": {
                    "should": [
                        { "match_phrase_prefix": {"name": "?0"}},
                        { "fuzzy": {"name": {"value": "?0", "fuzziness": "AUTO"}}}
                    ]
                }
            }
            """)
    Page<CategoryIndex> searchByName(String name, Pageable pageable);

    CategoryIndex findByName(String name);
}
