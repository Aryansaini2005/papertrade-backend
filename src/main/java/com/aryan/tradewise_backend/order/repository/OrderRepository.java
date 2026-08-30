package com.aryan.tradewise_backend.order.repository;

import com.aryan.tradewise_backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}