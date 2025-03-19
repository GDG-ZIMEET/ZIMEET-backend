package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.UserMatching;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserMatchingRepository extends JpaRepository<UserMatching, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT um FROM UserMatching um JOIN FETCH um.user u JOIN FETCH u.userProfile WHERE um.matching.id = :matchingId")
    List<UserMatching> findAllByMatchingIdWithUserProfile(@Param("matchingId") Long matchingId);

    UserMatching findByUserIdAndMatchingId(Long userId, Long matchingId);
}
