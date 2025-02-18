package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.TeamType;

import java.util.List;

public interface HiQueryService  {
        void sendHi(MeetingRequestDTO.hiDto hiDto);
        void refuseHi(MeetingRequestDTO.hiDto hiDto);
        List<MeetingResponseDTO.hiListDto> checkHiList(Long teamId, String action);

}
