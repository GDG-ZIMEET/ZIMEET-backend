package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.MatchingQueue;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchingQueueRepository extends JpaRepository<MatchingQueue, Long> {

    boolean existsByUserId(Long userId);

    @Query("SELECT q.groupId FROM MatchingQueue q GROUP BY q.groupId HAVING COUNT(q) < 4 ORDER BY MIN(q.createdAt)")
    Optional<String> findJoinableGroupId();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM MatchingQueue q WHERE q.groupId = :groupId")
    List<MatchingQueue> findByGroupIdWithLock(@Param("groupId") String groupId);
}