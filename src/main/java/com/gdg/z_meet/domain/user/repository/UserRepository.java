package com.gdg.z_meet.domain.user.repository;

import com.gdg.z_meet.domain.meeting.entity.enums.TeamType;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentNumber(String studentNumber);
    Optional<User> findById(Long userId);
    Optional<User> findByName(String name);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.id = :userId")
    User findByIdWithProfile(@Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.id IN :userIds")
    List<User> findAllByIdWithProfile(@Param("userIds") List<Long> userIds);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up WHERE up.gender = :gender " +
            "AND up.nickname LIKE :nickname% AND u.id != :userId " +
            "AND NOT EXISTS (SELECT 1 FROM Team t JOIN UserTeam ut ON ut.team = t " +
            "WHERE ut.user = u AND t.teamType = :teamType)")
    List<User> findAllByNicknameWithProfile(@Param("gender") Gender gender, @Param("nickname") String nickname, @Param("userId") Long userId, @Param("teamType") TeamType teamType);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up WHERE up.gender = :gender " +
            "AND u.phoneNumber LIKE :phoneNumber% AND u.id != :userId " +
            "AND NOT EXISTS (SELECT 1 FROM Team t JOIN UserTeam ut ON ut.team = t " +
            "WHERE ut.user = u AND t.teamType = :teamType)")
    List<User> findAllByPhoneNumberWithProfile(@Param("gender") Gender gender, @Param("phoneNumber") String phoneNumber, @Param("userId") Long userId, @Param("teamType") TeamType teamType);
}
