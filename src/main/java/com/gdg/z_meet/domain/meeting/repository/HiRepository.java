package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.enums.HiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface HiRepository extends JpaRepository<Hi,Long> {
    Boolean existsByFromAndToAndHiStatusNot(Team from, Team to, HiStatus status);
    Hi findByFromAndTo(Team from, Team to);
    @Query("SELECT DISTINCT h FROM Hi h WHERE h.to.id IN (:teamIds) AND h.hiStatus='NONE' ORDER BY h.createdAt DESC")
    List<Hi> findRecevieHiList(@Param("teamIds") List<Long> teamIds);
    @Query("SELECT DISTINCT h FROM Hi h WHERE h.from.id IN (:teamIds) AND h.hiStatus!='DELETED' ORDER BY h.createdAt DESC")
    List<Hi> findSendHiList(@Param("teamIds") List<Long> teamIds);
    @Modifying
    @Query("UPDATE Hi h SET h.hiStatus = 'DELETED' WHERE h.from.id = :teamId OR h.to.id = :teamId")
    void updateHiByTeamId(@Param("teamId") Long teamId);

    Boolean existsByFromAndTo(Team from, Team to);
}
