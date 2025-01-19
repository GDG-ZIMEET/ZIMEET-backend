package com.gdg.z_meet.domain.booth.service;

import com.gdg.z_meet.domain.booth.converter.ClubConverter;
import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.repository.ClubRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubCommandServiceImpl implements ClubCommandService {

    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public Club createClub(ClubRequestDTO.ClubCreateDTO request) {

        if (clubRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException(Code.CLUB_ALREADY_EXIST);
        }

        Club club = ClubConverter.toClub(request);

        return clubRepository.save(club);
    }
}