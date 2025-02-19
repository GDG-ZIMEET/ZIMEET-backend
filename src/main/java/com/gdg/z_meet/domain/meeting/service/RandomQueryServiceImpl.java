package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.converter.RandomConverter;
import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RandomQueryServiceImpl implements RandomQueryService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public RandomResponseDTO.GetTicketDTO getTicket(Long userId) {

        User user = userRepository.findByIdWithProfile(userId);

        return RandomConverter.toGetTicketDTO(user);
    }
}