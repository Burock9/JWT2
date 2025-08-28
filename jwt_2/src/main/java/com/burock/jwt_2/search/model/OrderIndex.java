package com.burock.jwt_2.search.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Document(indexName = "orders")
public class OrderIndex {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String orderNumber;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date)
    private LocalDateTime orderDate;

    @Field(type = FieldType.Date)
    private LocalDateTime deliveryDate;

    @Field(type = FieldType.Text)
    private String shippingAddress;

    @Field(type = FieldType.Text)
    private String notes;
}
