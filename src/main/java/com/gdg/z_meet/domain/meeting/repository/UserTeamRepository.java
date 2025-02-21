package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
    List<UserTeam> findByTeamId(Long teamId);
    List<UserTeam> findByTeamIdIn(List<Long> teamIds);
    Long countByTeamId(Long id);
    void deleteAllByTeamId(Long teamId);
    List<UserTeam> findByUserId(Long userId);

}