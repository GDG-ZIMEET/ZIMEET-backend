package com.gdg.z_meet.domain.user.repository;

import com.gdg.z_meet.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(String studentNumber);
    Optional<User> findById(Long userId);
    Optional<User> findByNickname(String nickname);
}
