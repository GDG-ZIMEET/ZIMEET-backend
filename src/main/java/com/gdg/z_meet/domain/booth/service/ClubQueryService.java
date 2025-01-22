package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;

import java.util.List;

public interface ClubQueryService {

    public Club getClub(Long clubId);
    public List<Club> getAllClub(String place);
}
