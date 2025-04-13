package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;

public interface HiCommandService {
    void sendHi(MeetingRequestDTO.hiDto hiDto);
    void sendUserHi(MeetingRequestDTO.hiDto hiDto);
    void sendTeamHi(MeetingRequestDTO.hiDto hiDto);
    void refuseHi(MeetingRequestDTO.hiDto hiDto);
}
