package com.gdg.z_meet.domain.order.repository;

import com.gdg.z_meet.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NamedLockRepository extends JpaRepository<Order, Long> {

    // 네임드 락 획득
    @Query(value = "SELECT GET_LOCK(:lockName, 10)", nativeQuery = true)
    Integer getLock(@Param("lockName") String lockName);

    // 네임드 락 해제
    @Query(value = "SELECT RELEASE_LOCK(:lockName)", nativeQuery = true)
    Integer releaseLock(@Param("lockName") String lockName);
}
