package com.gdg.z_meet.domain.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.meeting.converter.RandomConverter;
import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Matching;
import com.gdg.z_meet.domain.meeting.entity.UserMatching;
import com.gdg.z_meet.domain.meeting.entity.enums.MatchingStatus;
import com.gdg.z_meet.domain.meeting.repository.MatchingRepository;
import com.gdg.z_meet.domain.meeting.repository.UserMatchingRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RandomCommandServiceImpl implements RandomCommandService {

    private final SimpMessagingTemplate messagingTemplate;

    @Qualifier("matchingRedisTemplate")
    private final RedisTemplate<String, String> matchingRedisTemplate;
    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createMatching(Long userId) {

        // 1. 대기 중인 매칭룸 찾기
        Matching matching = matchingRepository.findWaitingMatching()
                .orElseGet(() -> matchingRepository.save(Matching.builder().build()));

        // 2. 유저를 매칭룸에 추가
        UserMatching userMatching = UserMatching.builder()
                .user(userRepository.findByIdWithProfile(userId))
                .matching(matching)
                .build();
        userMatchingRepository.save(userMatching);

        validateMatching(matching);
    }

    private void validateMatching(Matching matching) {

        List<UserMatching> matchedUsers = userMatchingRepository.findAllByMatchingIdWithUserProfile(matching.getId());

        long male = matchedUsers.stream()
                .filter(user -> user.getUser().getUserProfile().getGender() == Gender.MALE)
                .count();
        long female = matchedUsers.stream()
                .filter(user -> user.getUser().getUserProfile().getGender() == Gender.FEMALE)
                .count();

        // 3. 매칭 조건 확인
        if (male == 3 && female == 3) {
            // 매칭 완료 처리
            matching.setMatchingStatus(MatchingStatus.COMPLETE);
        }

        List<UserMatching> userMatchings = userMatchingRepository.findByMatchingId(matching.getId());
        List<User> users = userMatchings.stream()
                .map(UserMatching::getUser)
                .collect(Collectors.toList());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(
                    RandomConverter.toMatchingDTO(matching, users)
            );
            matchingRedisTemplate.convertAndSend("matching", jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleMessage(String message) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            if (message.startsWith("\"") && message.endsWith("\"")) {
                message = message.substring(1, message.length() - 1);
                message = message.replace("\\\"", "\"");
            }

            RandomResponseDTO.MatchingDTO dto = objectMapper.readValue(message,
                    RandomResponseDTO.MatchingDTO.class);
            messagingTemplate.convertAndSend("/topic/matching", dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}