package com.gdg.z_meet.domain.event.service;

import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final UserRepository userRepository;

    @Transactional
    public MeetingResponseDTO.GetMyDeleteDTO patchMyDelete(String name, String phoneNumber) {

        User user = userRepository.findByNameAndPhoneNumberWithProfile(name, phoneNumber)
                .orElseThrow(() -> new BusinessException(Code.USER_NOT_FOUND));

        user.getUserProfile().addDelete();

        return MeetingConverter.toGetMyDeleteDTO(user);
    }
}
