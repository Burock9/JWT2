package com.burock.jwt_2.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.burock.jwt_2.model.Order;
import com.burock.jwt_2.model.OrderStatus;
import com.burock.jwt_2.model.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserOrderDateDesc(User user);

    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDated(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user")
    Long countOrdersByUser(@Param("user") User user);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = :status")
    List<Order> findByUserAndStatus(@Param("user") User user, @Param("status") OrderStatus status);

}
