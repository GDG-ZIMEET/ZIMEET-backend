package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.UserMatching;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMatchingRepository extends JpaRepository<UserMatching, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT um FROM UserMatching um JOIN FETCH um.user u JOIN FETCH u.userProfile WHERE um.matching.id = :matchingId")
    List<UserMatching> findAllByMatchingIdWithUserProfile(@Param("matchingId") Long matchingId);

    UserMatching findByUserIdAndMatchingId(Long userId, Long matchingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserMatching u WHERE u.id = :id")
    Optional<UserMatching> findByIdForUpdate(@Param("id") Long id);
}
