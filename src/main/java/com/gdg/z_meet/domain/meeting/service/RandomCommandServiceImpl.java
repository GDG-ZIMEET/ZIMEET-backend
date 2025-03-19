package com.gdg.z_meet.domain.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.chat.service.ChatRoomCommandService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RandomCommandServiceImpl implements RandomCommandService {

    @Qualifier("matchingRedisTemplate")
    private final RedisTemplate<String, String> matchingRedisTemplate;
    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final UserRepository userRepository;
    private final ChatRoomCommandService chatRoomCommandService;

    @Autowired
    private MatchingLockService matchingLockService;


    @Override
    @Transactional
    public void joinMatching(Long userId) {

        User user = userRepository.findByIdWithProfile(userId);
        if (user.getUserProfile().getTicket() == 0) {
            throw new BusinessException(Code.TICKET_LIMIT_EXCEEDED);
        }

        // 늘품제용 티켓 무제한 설정
        //user.getUserProfile().decreaseTicket(1);

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

        validateMatching(matching, userMatchings);
        RandomResponseDTO.MatchingDTO matchingDTO = messageMatching(matching, userMatchings);

        List<Long> userIds = userMatchings.stream()
                .map(um -> um.getUser().getId())
                .collect(Collectors.toList());

        if (matching.getMatchingStatus() == MatchingStatus.COMPLETE) {
            chatRoomCommandService.addUserJoinChat(userIds);
        }
    }

    @Override
    @Transactional
    public void cancelMatching(Long userId) {

        // 완료된 매칭은 취소 불가
        Matching matching = matchingRepository.findWaitingMatchingByUserId(userId)
                .orElseThrow(() -> new BusinessException(Code.MATCHING_NOT_FOUND));

        userMatchingRepository.findByIdForUpdate(userId).ifPresent(this::safeDeleteUserMatching);

        userRepository.findByIdWithProfile(userId);

        // 늘품제용 티켓 무제한 설정
        //user.getUserProfile().increaseTicket(1);

        List<UserMatching> userMatchings = userMatchingRepository.findAllByMatchingIdWithUserProfile(matching.getId());
        messageMatching(matching, userMatchings);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void safeDeleteUserMatching(UserMatching userMatching) {
        userMatchingRepository.findById(userMatching.getId()).ifPresent(userMatchingRepository::delete);
    }


    private RandomResponseDTO.MatchingDTO messageMatching(Matching matching, List<UserMatching> userMatchings) {

        List<User> users = userMatchings.stream()
                .map(UserMatching::getUser)
                .collect(Collectors.toList());

        RandomResponseDTO.MatchingDTO matchingDTO = RandomConverter.toMatchingDTO(matching, users);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(matchingDTO);

            String channel = "matching." + matchingDTO.getMatchingId();
            matchingRedisTemplate.convertAndSend(channel, jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return matchingDTO;
    }

    private void validateMatching(Matching matching, List<UserMatching> userMatchings) {

        matchingLockService.lock(matching.getId());

        try {
            long male = userMatchings.stream()
                    .filter(user -> user.getUser().getUserProfile().getGender() == Gender.MALE)
                    .count();
            long female = userMatchings.stream()
                    .filter(user -> user.getUser().getUserProfile().getGender() == Gender.FEMALE)
                    .count();

            if (male == 3 && female == 3) {
                matching.setMatchingStatus(MatchingStatus.COMPLETE);
            }
        } finally {
            matchingLockService.unlock(matching.getId());
        }
    }
}