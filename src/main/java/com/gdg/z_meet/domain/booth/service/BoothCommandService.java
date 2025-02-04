package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.dto.BoothRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;

public interface BoothCommandService {

    Club createClub(BoothRequestDTO.CreateClubDTO request);
}
