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
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
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
    public void joinMatching(Long userId) {

        User user = userRepository.findByIdWithProfile(userId);
        if (user.getUserProfile().getTicket() == 0) {
            throw new BusinessException(Code.TICKET_LIMIT_EXCEEDED);
        }
        user.getUserProfile().decreaseTicket(1);

        // 진행중인 매칭 확인
        if (matchingRepository.existsByWaitingMatching(userId)) {
            throw new BusinessException(Code.MATCHING_ALREADY_EXIST);
        }

        Gender gender = user.getUserProfile().getGender();
        Matching matching = matchingRepository.findFirstWaitingMatching(userId)
                .filter(m -> {
                    List<UserMatching> userMatchings = userMatchingRepository.findAllByMatchingIdWithUserProfile(m.getId());
                    long genderCount = userMatchings.stream()
                            .filter(userMatching -> userMatching.getUser().getUserProfile().getGender() == gender)
                            .count();
                    return genderCount < 3;
                })
                .orElseGet(() -> matchingRepository.save(Matching.builder().build()));

        UserMatching userMatching = UserMatching.builder()
                .user(user)
                .matching(matching)
                .build();
        userMatchingRepository.save(userMatching);

        List<UserMatching> userMatchings = userMatchingRepository.findAllByMatchingIdWithUserProfile(matching.getId());

        messageMatching(matching, userMatchings);
        validateMatching(matching, userMatchings);
    }

    @Override
    @Transactional
    public void cancelMatching(Long userId) {

        // 완료된 매칭은 취소 불가
        Matching matching = matchingRepository.findWaitingMatchingByUserId(userId)
                .orElseThrow(() -> new BusinessException(Code.MATCHING_NOT_FOUND));

        UserMatching userMatching = userMatchingRepository.findByUserIdAndMatchingId(userId, matching.getId());
        userMatchingRepository.delete(userMatching);

        User user = userRepository.findByIdWithProfile(userId);
        user.getUserProfile().increaseTicket(1);

        List<UserMatching> userMatchings = userMatchingRepository.findAllByMatchingIdWithUserProfile(matching.getId());
        messageMatching(matching, userMatchings);
    }

    private void messageMatching(Matching matching, List<UserMatching> userMatchings) {

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

    private void validateMatching(Matching matching, List<UserMatching> userMatchings) {

        long male = userMatchings.stream()
                .filter(user -> user.getUser().getUserProfile().getGender() == Gender.MALE)
                .count();
        long female = userMatchings.stream()
                .filter(user -> user.getUser().getUserProfile().getGender() == Gender.FEMALE)
                .count();

        if (male == 3 && female == 3) {
            matching.setMatchingStatus(MatchingStatus.COMPLETE);
        }
    }
}