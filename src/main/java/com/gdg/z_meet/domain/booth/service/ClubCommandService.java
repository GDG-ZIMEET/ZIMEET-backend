package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;

public interface ClubCommandService {

    public Club createClub(ClubRequestDTO.ClubCreateDTO request);
}
