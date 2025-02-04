package com.gdg.z_meet.domain.order.repository;

import com.gdg.z_meet.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, byte[]> {

    Optional<Order> findById(UUID orderId);
}
