package com.gdg.z_meet.domain.meeting.repository;

import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HiRepository extends JpaRepository<Hi,Long> {
    Boolean existsByFromAndTo(Team from, Team to);
    Hi findByFromAndTo(Team from, Team to);
}
