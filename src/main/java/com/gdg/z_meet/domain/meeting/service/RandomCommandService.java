package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;

public interface RandomCommandService {

    RandomResponseDTO.MatchingDTO joinMatching(Long userId);
    void cancelMatching(Long userId);
}
