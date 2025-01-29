package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.user.entity.User;

import java.util.List;

public interface MeetingQueryService {

    public Team getTeam(Long teamId);
    public List<User> getUserTeam(Long teamId);
}
