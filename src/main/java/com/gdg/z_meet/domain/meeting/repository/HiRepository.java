package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface HiRepository extends JpaRepository<Hi,Long> {
    Boolean existsByFromAndTo(Team from, Team to);
    Hi findByFromAndTo(Team from, Team to);
    @Query("SELECT h FROM Hi h WHERE h.to.id = :teamId AND h.hiStatus='NONE' ORDER BY h.createdAt DESC")
    List<Hi> findRecevieHiList(Long teamId);
    @Query("SELECT h FROM Hi h WHERE h.from.id = :teamId ORDER BY h.createdAt DESC")
    List<Hi> findSendHiList(Long teamId);
}
