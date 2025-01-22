package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.converter.ClubConverter;
import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.entity.Place;
import com.gdg.z_meet.domain.booth.repository.ClubRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubRepository clubRepository;

    @Override
    public Club getClub(Long clubId) {

        return clubRepository.findById(clubId).orElseThrow(() -> new BusinessException(Code.CLUB_NOT_FOUND));
    }

    @Override
    public List<Club> getAllClub(String place) {
        return clubRepository.findByPlace(Place.valueOf(place));
    }
}