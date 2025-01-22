package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;

public interface ClubQueryService {

    public Club getClub(Long clubId);
}
