package com.gdg.z_meet.domain.meeting.converter;

import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Matching;
import com.gdg.z_meet.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class RandomConverter {

    public static RandomResponseDTO.GetTicketDTO toGetTicketDTO(User user){

        return RandomResponseDTO.GetTicketDTO.builder()
                .ticket(user.getUserProfile().getTicket())
                .build();
    }

    public static RandomResponseDTO.MatchingDTO toMatchingDTO(Matching matching, List<User> users){

        List<RandomResponseDTO.UserMatchingDTO> userMatchingDTOS = users.stream()
                .map(user -> RandomResponseDTO.UserMatchingDTO.builder()
                        .userId(user.getId())
                        .emoji(user.getUserProfile().getEmoji())
                        .gender(String.valueOf(user.getUserProfile().getGender()))
                        .build())
                .collect(Collectors.toList());

        return RandomResponseDTO.MatchingDTO.builder()
                .matchingId(matching.getId())
                .userList(userMatchingDTOS)
                .matchingStatus(String.valueOf(matching.getMatchingStatus()))
                .build();
    }
}
