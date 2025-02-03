package com.gdg.z_meet.domain.order.service;

import com.gdg.z_meet.domain.order.dto.ConfirmPaymentRes;
import com.gdg.z_meet.domain.order.dto.ConfirmSuccessPaymentInfo;
import com.gdg.z_meet.domain.order.entity.Order;
import com.gdg.z_meet.domain.order.entity.TossPayment;
import com.gdg.z_meet.domain.order.entity.TossPaymentStatus;
import com.gdg.z_meet.domain.order.repository.OrderRepository;
import com.gdg.z_meet.domain.order.repository.TossPaymentRepository;
import com.gdg.z_meet.global.Util.UUIDUtil;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Toss Payments 와 연관된 내부 로직 처리 Service
 */
@Service
@AllArgsConstructor
public class TossPaymentService {

    private final TossPaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // 결제 정보 조회(UUID)  - 추후 사용 예정
    public ConfirmPaymentRes getPayment(String backendOrderId) {
        TossPayment tossPayment = paymentRepository.findByOrder_OrderId(UUIDUtil.hexStringToByteArray(backendOrderId)).orElseThrow(() -> new BusinessException(Code.INVALID_PAYMENT_REQUEST));
        return tossPayment.toResponse();
    }

    // 주문 검증 -> 결제 정보 저장
    @Transactional
    public ConfirmPaymentRes addPayment(ConfirmSuccessPaymentInfo confirmSuccessPaymentInfo) {
        Order order = orderRepository.findById(UUIDUtil.hexStringToByteArray(confirmSuccessPaymentInfo.getBackendOrderId())).orElseThrow(() -> new BusinessException(Code.INVALID_ORDER));
        return paymentRepository.save(confirmSuccessPaymentInfo.toTossPayment(order)).toResponse();
    }

    // 결제 상태 변경
    @Transactional
    public void changePaymentStatus(String paymentKey, TossPaymentStatus tossPaymentStatus) {
        TossPayment tossPayment = paymentRepository.findByTossPaymentKey(paymentKey).orElseThrow(() -> new BusinessException(Code.INVALID_PAYMENT_REQUEST));
        tossPayment.changePaymentStatus(tossPaymentStatus);
    }

}