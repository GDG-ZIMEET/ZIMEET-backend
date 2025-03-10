package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.enums.TeamType;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.id NOT IN (SELECT ut.team.id FROM UserTeam ut WHERE ut.user.id = :userId) " +
            "AND t.gender != :gender AND t.teamType = :teamType")
    List<Team> findAllByTeamType(@Param("userId") Long userId, @Param("gender") Gender gender, @Param("teamType") TeamType teamType, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.id IN (SELECT ut.team.id FROM UserTeam ut WHERE ut.user.id = :userId) " +
            "AND t.teamType = :teamType")
    Optional<Team> findByTeamType(@Param("userId") Long userId, @Param("teamType") TeamType teamType);

    Boolean existsByName(String name);

    List<Team> findByIdIn(List<Long> teamIds);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t " +
            "JOIN UserTeam ut ON ut.team = t " +
            "WHERE ut.user.id IN :userIds AND t.teamType = :teamType")
    Boolean existsAnyMemberByTeamType(@Param("userIds") List<Long> userIds, @Param("teamType") TeamType teamType);
}
