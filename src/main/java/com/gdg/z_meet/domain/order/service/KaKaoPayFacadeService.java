package com.gdg.z_meet.domain.order.service;


import com.gdg.z_meet.domain.order.dto.KaKaoPayApproveDTO;
import com.gdg.z_meet.domain.order.dto.KaKaoPayReadyDTO;
import com.gdg.z_meet.domain.order.repository.NamedLockRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KaKaoPayFacadeService {

    private final NamedLockRepository namedLockRepository;
    private final KaKaoPayService kakaoPayService;

    @Transactional
    public KaKaoPayReadyDTO.Response KaKaoPayReady(KaKaoPayReadyDTO.Parameter parameter) {
        return kakaoPayService.KaKaoPayReady(parameter);
    }

    @Transactional
    public KaKaoPayApproveDTO.Response KaKaoPayApprove(KaKaoPayApproveDTO.Parameter parameter) {
        String lockName = "LOCK_KAKAO_PAY_APPROVE_" + parameter.getOrderId();
        try {
            acquireLock(lockName);
            return kakaoPayService.KaKaoPayApprove(parameter);
        } finally {
            releaseLock(lockName);
        }
    }

    private void acquireLock(String lockName) {
        log.info("락 요청 lockName : {}", lockName);
        Integer result = namedLockRepository.getLock(lockName);
        if (result == null || result == 0) { throw new BusinessException(Code.INTERNAL_SERVER_ERROR);}
    }

    private void releaseLock(String lockName) {
        log.info("락 반환 lockName : {}", lockName);
        namedLockRepository.releaseLock(lockName);
    }
}

