package com.gdg.z_meet.domain.meeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void handleMessage(String message) {

        try {
            RandomResponseDTO.MatchingDTO updateMessage =
                    objectMapper.readValue(message, RandomResponseDTO.MatchingDTO.class);
            messagingTemplate.convertAndSend("/topic/matching", updateMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
