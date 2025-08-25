package com.burock.jwt_2.search.model;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "carts")
public class CartIndex {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text)
    private String userName;

    @Field(type = FieldType.Nested)
    private List<CartItemIndex> items;

    @Field(type = FieldType.Integer)
    private int totalItems;

    @Field(type = FieldType.Double)
    private double totalPrice;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemIndex {

        @Field(type = FieldType.Keyword)
        private String productId;

        @Field(type = FieldType.Text)
        private String productName;

        @Field(type = FieldType.Double)
        private double productPrice;

        @Field(type = FieldType.Integer)
        private int quantity;

        @Field(type = FieldType.Double)
        private double subtotal;

        @Field(type = FieldType.Keyword)
        private String categoryId;

        @Field(type = FieldType.Text)
        private String categoryName;
    }
}
