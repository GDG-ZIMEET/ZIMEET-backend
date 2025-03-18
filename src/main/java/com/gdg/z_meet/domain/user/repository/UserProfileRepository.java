package com.gdg.z_meet.domain.user.repository;

import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    List<UserProfile> findByUserIn(List<User> users);
    Optional<UserProfile> findByNickname(String nickname);
    Optional<UserProfile> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserProfile up SET up.leftDelete = up.leftDelete - 1 WHERE up.user.id IN :userIds")
    void subtractDelete(@Param("userIds") List<Long> userIds);

    Optional<UserProfile> findByUser(User user);
  
    @Query("SELECT up FROM UserProfile up JOIN up.user u JOIN UserTeam ut ON u.id = ut.user.id WHERE ut.team.id = :teamId")
    List<UserProfile> findByTeamId(Long teamId);

    boolean existsByNickname(String nickname);

    void deleteByUserId(Long userId);
}