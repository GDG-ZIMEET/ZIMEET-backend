package com.gdg.z_meet.domain.meeting.converter;

import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.user.entity.User;

public class RandomConverter {

    public static RandomResponseDTO.GetTicketDTO toGetTicketDTO(User user){

        return RandomResponseDTO.GetTicketDTO.builder()
                .ticket(user.getUserProfile().getTicket())
                .build();
    }
}
