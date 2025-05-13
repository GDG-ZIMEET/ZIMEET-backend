package com.gdg.z_meet.domain.user.repository;

import com.gdg.z_meet.domain.meeting.entity.enums.TeamType;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentNumber(String studentNumber);
    Optional<User> findById(Long userId);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.id = :userId AND u.isDeleted = false")
    User findByIdWithProfile(@Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.id IN :userIds")
    List<User> findAllByIdWithProfile(@Param("userIds") List<Long> userIds);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up WHERE up.gender = :gender " +
            "AND up.nickname LIKE :nickname% AND u.id != :userId AND u.isDeleted = false " +
            "AND NOT EXISTS (SELECT 1 FROM Team t JOIN UserTeam ut ON ut.team = t " +
            "WHERE ut.user = u AND t.teamType = :teamType AND t.activeStatus = 'ACTIVE')")
    List<User> findAllByNicknameWithProfile(@Param("gender") Gender gender, @Param("nickname") String nickname, @Param("userId") Long userId, @Param("teamType") TeamType teamType);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up WHERE up.gender = :gender " +
            "AND u.phoneNumber LIKE :phoneNumber% AND u.id != :userId AND u.isDeleted = false " +
            "AND NOT EXISTS (SELECT 1 FROM Team t JOIN UserTeam ut ON ut.team = t " +
            "WHERE ut.user = u AND t.teamType = :teamType AND t.activeStatus = 'ACTIVE')")
    List<User> findAllByPhoneNumberWithProfile(@Param("gender") Gender gender, @Param("phoneNumber") String phoneNumber, @Param("userId") Long userId, @Param("teamType") TeamType teamType);

    boolean existsByStudentNumber(String studentNumber);
    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.name = :name AND u.phoneNumber = :phoneNumber")
    Optional<User> findByNameAndPhoneNumberWithProfile(String name, String phoneNumber);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.name = :name AND u.studentNumber = :studentNumber")
    Optional<User> findByNameAndStudentNumberWithProfile(String name, String studentNumber);

    Optional<User> findByNameAndStudentNumberAndPhoneNumber(String name, String studentNumber, String phoneNumber);

    List<User> findByIdIn(List<Long> userIds);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up " +
            "WHERE u.id != :userId AND up.gender != :gender AND up.profileStatus = 'ACTIVE' AND u.isDeleted = false " +
            "ORDER BY FUNCTION('RAND')")
    List<User> findAllByProfileStatus(@Param("userId") Long userId, @Param("gender") Gender gender, Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.userProfile up " +
            "WHERE u.id = :userId AND up.profileStatus = 'ACTIVE' AND u.isDeleted = false")
    Optional<User> findByProfileStatus(@Param("userId") Long userId);
}
