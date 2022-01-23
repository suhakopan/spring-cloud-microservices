package com.suhakopan.springexamples.orderservice.repository;

import com.suhakopan.springexamples.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderRepository extends JpaRepository<Order, Long> {
}
