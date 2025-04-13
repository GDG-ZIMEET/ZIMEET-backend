package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.enums.HiStatus;
import com.gdg.z_meet.domain.meeting.entity.enums.HiType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface HiRepository extends JpaRepository<Hi,Long> {
    Boolean existsByFromIdAndToIdAndHiStatusNotAndHiType(Long from, Long to, HiStatus status, HiType hiType);
    Hi findByFromIdAndToId(Long from, Long to);
    @Query("SELECT DISTINCT h FROM Hi h WHERE h.toId IN (:teamIds) AND h.hiStatus='NONE' ORDER BY h.createdAt DESC")
    List<Hi> findRecevieHiList(@Param("teamIds") List<Long> teamIds);
    @Query("SELECT DISTINCT h FROM Hi h WHERE h.fromId IN (:teamIds) AND h.hiStatus!='DELETED' ORDER BY h.createdAt DESC")
    List<Hi> findSendHiList(@Param("teamIds") List<Long> teamIds);
    @Modifying
    @Query("UPDATE Hi h SET h.hiStatus = 'DELETED' WHERE h.fromId = :teamId OR h.toId = :teamId")
    void updateHiByTeamId(@Param("teamId") Long teamId);

    Boolean existsByFromIdAndToIdAndHiType(Long fromId, Long toId, HiType hiType);

}
