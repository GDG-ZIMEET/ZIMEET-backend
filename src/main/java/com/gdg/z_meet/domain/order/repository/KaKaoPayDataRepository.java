package com.gdg.z_meet.domain.order.repository;

import com.gdg.z_meet.domain.order.entity.KaKaoPayData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KaKaoPayDataRepository extends JpaRepository<KaKaoPayData, Long> {
    Optional<KaKaoPayData> findByOrderId(String orderId);
}
