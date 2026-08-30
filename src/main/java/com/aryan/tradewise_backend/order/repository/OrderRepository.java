package com.aryan.tradewise_backend.order.repository;

import com.aryan.tradewise_backend.order.entity.Order;
import com.aryan.tradewise_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);
}