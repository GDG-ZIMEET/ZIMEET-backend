package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.converter.BoothConverter;
import com.gdg.z_meet.domain.booth.dto.BoothRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoothCommandServiceImpl implements BoothCommandService {

    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public Club createClub(BoothRequestDTO.CreateClubDTO request) {

        Club club = BoothConverter.toClub(request);
        return clubRepository.save(club);
    }
}