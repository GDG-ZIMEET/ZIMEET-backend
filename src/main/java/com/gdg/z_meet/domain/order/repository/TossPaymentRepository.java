package com.gdg.z_meet.domain.order.repository;

import com.gdg.z_meet.domain.order.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossPaymentRepository extends JpaRepository<TossPayment, byte[]> {

    // 연관된 엔티티 탐색
    Optional<TossPayment> findByOrder_OrderId(byte[] orderId);
    Optional<TossPayment> findByTossPaymentKey(String tossPaymentKey);

}
