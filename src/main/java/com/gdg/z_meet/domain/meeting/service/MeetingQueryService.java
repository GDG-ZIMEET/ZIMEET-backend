package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.entity.Team;

public interface MeetingQueryService {

    public Team getTeam(Long teamId);
}
