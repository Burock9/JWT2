package com.burock.jwt_2.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.burock.jwt_2.model.Order;
import com.burock.jwt_2.search.model.OrderIndex;
import com.burock.jwt_2.search.repository.OrderSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSearchService {

    private final OrderSearchRepository orderSearchRepository;

    public void indexOrder(Order order) {
        OrderIndex doc = OrderIndex.builder()
                .id(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId().toString())
                .username(order.getUser().getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .build();

        orderSearchRepository.save(doc);
    }

    public Page<OrderIndex> getAll(Pageable pageable) {
        return orderSearchRepository.findAll(pageable);
    }

    public Page<OrderIndex> searchOrders(String query, Pageable pageable) {
        return orderSearchRepository.searchByText(query, pageable);
    }

    public Page<OrderIndex> findByStatus(String status, Pageable pageable) {
        return orderSearchRepository.findByStatus(status, pageable);
    }

    public Page<OrderIndex> findByUsername(String username, Pageable pageable) {
        return orderSearchRepository.findByUsername(username, pageable);
    }

    public void deleteFromIndex(Long orderId) {
        orderSearchRepository.deleteById(orderId.toString());
    }
}
