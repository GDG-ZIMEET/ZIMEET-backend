package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.user.entity.User;

import java.util.List;

public interface MeetingQueryService {

    Team getTeam(Long userId, Long teamId);
    List<User> getUserTeam(Long teamId);
}
