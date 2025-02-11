package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.id NOT IN (SELECT ut.team.id FROM UserTeam ut WHERE ut.user.id = :userId) " +
            "AND t.gender != :gender AND t.teamType = :teamType")
    List<Team> findAllByTeamType(Long userId, Gender gender, TeamType teamType, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.id IN (SELECT ut.team.id FROM UserTeam ut WHERE ut.user.id = :userId) " +
            "AND t.teamType = :teamType")
    Optional<Team> findByTeamType(Long userId, TeamType teamType);

    Boolean existsByName(String name);
}
