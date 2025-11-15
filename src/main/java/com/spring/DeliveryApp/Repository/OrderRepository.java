package com.spring.DeliveryApp.Repository;

import com.spring.DeliveryApp.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}