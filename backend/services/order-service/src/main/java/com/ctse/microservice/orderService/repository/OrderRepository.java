package com.ctse.microservice.orderService.repository;

import com.ctse.microservice.orderService.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
