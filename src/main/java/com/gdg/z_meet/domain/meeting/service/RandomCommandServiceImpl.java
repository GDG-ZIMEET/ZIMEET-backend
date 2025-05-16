package com.gdg.z_meet.domain.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.chat.service.ChatRoomCommandService;
import com.gdg.z_meet.domain.meeting.converter.RandomConverter;
import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Matching;
import com.gdg.z_meet.domain.meeting.entity.MatchingQueue;
import com.gdg.z_meet.domain.meeting.entity.UserMatching;
import com.gdg.z_meet.domain.meeting.repository.MatchingQueueRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RandomCommandServiceImpl implements RandomCommandService {

    @Qualifier("matchingRedisTemplate")
    private final RedisTemplate<String, String> matchingRedisTemplate;
    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final MatchingQueueRepository matchingQueueRepository;
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

        if (matchingQueueRepository.existsByUserId(userId)) {
            throw new BusinessException(Code.MATCHING_ALREADY_EXIST);
        }

        String groupId = matchingQueueRepository.findJoinableGroupId()
                .orElse(UUID.randomUUID().toString());
        Gender gender = user.getUserProfile().getGender();

        MatchingQueue queue = MatchingQueue.builder()
                .groupId(groupId)
                .gender(gender)
                .user(user)
                .build();
        matchingQueueRepository.save(queue);

        user.getUserProfile().decreaseTicket(1);

        createMatching(groupId);
    }

    @Override
    @Transactional
    public void cancelMatching(Long userId) {

        Matching matching = matchingRepository.findWaitingMatchingByUserId(userId)
                .orElseThrow(() -> new BusinessException(Code.MATCHING_NOT_FOUND));

        userMatchingRepository.findByUserIdForUpdate(userId).ifPresent(this::safeDeleteUserMatching);

        User user = userRepository.findByIdWithProfile(userId);

        user.getUserProfile().increaseTicket(1);

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

    @Transactional
    public void createMatching(String groupId) {

        List<MatchingQueue> queueList = matchingQueueRepository.findByGroupIdWithLock(groupId);

        long male = queueList.stream().filter(q -> q.getGender() == Gender.MALE).count();
        long female = queueList.stream().filter(q -> q.getGender() == Gender.FEMALE).count();

        if (male == 2 && female == 2) {

            Matching matching = matchingRepository.save(Matching.builder().build());

            queueList.forEach(queue -> {
                userMatchingRepository.save(UserMatching.builder()
                        .user(queue.getUser())
                        .matching(matching)
                        .build());
                matchingQueueRepository.delete(queue);
            });

            List<UserMatching> userMatchingList = userMatchingRepository.findAllByMatchingIdWithUserProfile(matching.getId());
            messageMatching(matching, userMatchingList);

            List<Long> userIds = userMatchingList.stream()
                    .map(um -> um.getUser().getId())
                    .collect(Collectors.toList());
            chatRoomCommandService.addRandomUserJoinChat(userIds);
        }
    }
}