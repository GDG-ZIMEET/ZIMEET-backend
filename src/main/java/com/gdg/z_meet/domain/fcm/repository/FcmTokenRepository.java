package com.gdg.z_meet.domain.fcm.repository;

import com.gdg.z_meet.domain.fcm.entity.FcmToken;
import com.gdg.z_meet.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByUser(User user);

    void deleteByUser(User user);

    boolean existsByUserAndToken(User user, String token);

    List<FcmToken> findAllByUser(User user);

    void deleteByToken(String token);
}
