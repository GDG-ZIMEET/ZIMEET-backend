package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.JoinChat;
import com.gdg.z_meet.domain.chat.entity.TeamChatRoom;
import com.gdg.z_meet.domain.chat.entity.status.ChatType;
import com.gdg.z_meet.domain.chat.entity.status.JoinChatStatus;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.TeamChatRoomRepository;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.UserProfile;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TeamChatRoomRepository teamChatRoomRepository;
    private final UserTeamRepository userTeamRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";

    // 채팅방 ID로 채팅방 조회 (Redis 캐싱)
    public ChatRoom getChatRoomById(Long chatRoomId) {
        Object chatRoomObj = redisTemplate.opsForHash().get("chatrooms", chatRoomId.toString());
        if (chatRoomObj instanceof ChatRoom) {
            return (ChatRoom) chatRoomObj;
        }
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));
        redisTemplate.opsForHash().put("chatrooms", chatRoomId.toString(), chatRoom);
        return chatRoom;
    }

    // 사용자 채팅방 목록 조회 (최신 활동 기준 정렬)
    @Transactional
    public List<ChatRoomDto.chatRoomListDto> getChatRoomsByUser(Long userId) {
        String joinChatsKey = "user:" + userId + ":chatrooms";

        // Redis에서 참여 채팅방 ID 가져오기 및 변환
        Set<Long> joinChatIdsSet = redisTemplate.opsForSet()
                .members(joinChatsKey)
                .stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .collect(Collectors.toSet());


        if (joinChatIdsSet == null || joinChatIdsSet.isEmpty()) {
            List<JoinChat> joinChats = joinChatRepository.findByUserIdAndStatus(userId, JoinChatStatus.ACTIVE);
            joinChatIdsSet = joinChats.stream()
                    .map(joinChat -> joinChat.getChatRoom().getId())
                    .collect(Collectors.toSet());
            joinChatIdsSet.forEach(chatRoomId -> redisTemplate.opsForSet().add(joinChatsKey, chatRoomId));
        }


        // Redis ZSet에서 최신 활동 기준으로 정렬된 채팅방 ID 가져오기
        List<Long> sortedChatRoomIds = Optional.ofNullable(
                        redisTemplate.opsForZSet().reverseRange(CHAT_ROOM_LATEST_MESSAGE_KEY, 0, -1))
                .orElse(Set.of()) // null 방지
                .stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .filter(joinChatIdsSet::contains)
                .collect(Collectors.toList());

        // 정렬된 목록에 없는 채팅방 추가
        joinChatIdsSet.stream()
                .filter(id -> !sortedChatRoomIds.contains(id)) // 정렬되지 않은 채팅방 추가
                .forEach(sortedChatRoomIds::add);


        return sortedChatRoomIds.stream()
                .map(chatRoomId -> {

                    ChatRoom chatRoom = getChatRoomById(chatRoomId);
                    String chatRoomName = getChatRoomName(userId, chatRoom.getId());

                    // 최신 메시지 및 메시지 시간 조회
                    String latestMessageKey = String.format(CHAT_ROOM_LATEST_MESSAGE_KEY, chatRoomId);
                    String latestMessageTimeKey = String.format("chatroom:%s:latestMessageTime", chatRoomId);

                    String latestMessage = (String) redisTemplate.opsForValue().get(latestMessageKey);
                    String latestMessageTimeStr = (String) redisTemplate.opsForValue().get(latestMessageTimeKey);

                    LocalDateTime latestMessageTime = null;
                    if (latestMessageTimeStr != null) {
                        latestMessageTime = LocalDateTime.parse(latestMessageTimeStr);
                    }
                    else{
                        latestMessageTime = chatRoom.getUpdatedAt();
                    }

                    List<ChatRoomDto.UserProfileDto> userProfiles = getUserProfilesByChatRoomId(userId, chatRoomId, true);


                    return new ChatRoomDto.chatRoomListDto(chatRoom.getId(), chatRoomName, latestMessage, latestMessageTime, userProfiles);
                })
                .collect(Collectors.toList());
    }


    // 채팅방 이름 조회
    @Transactional
    public String getChatRoomName(Long userId, Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));

        if (chatRoom.getChatType() == ChatType.RANDOM) {
            // 랜덤 채팅방이면 고유한 ID 기반으로 이름 생성
            return "랜덤채팅 " + (chatRoom.getRandomChatId() +30) + "번방";
        }

        // 해당 채팅방의 팀 조회
        List<TeamChatRoom> teamChatRooms = teamChatRoomRepository.findByChatRoomId(chatRoomId);

        // 현재 사용자가 속한 팀 찾기
        TeamChatRoom userTeamChatRoom = teamChatRooms.stream()
                .filter(teamChatRoom ->
                        userTeamRepository.existsByUserIdAndTeamId(userId, teamChatRoom.getTeam().getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(Code.JOINCHAT_NOT_FOUND));

        // 상대팀 이름 리턴
        return userTeamChatRoom.getName();
    }

    @Transactional
    public List<ChatRoomDto.UserProfileDto> getUserProfilesByChatRoomId(Long userId, Long chatRoomId, boolean filterByGender) {
        // 현재 사용자의 성별 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
        Gender userGender = user.getUserProfile().getGender(); // 성별 가져오기

        // 채팅방 전체 사용자 조회
        List<User> users = joinChatRepository.findUsersByChatRoomId(chatRoomId);

        // filterByGender가 true일 경우, 이성만 남김
        if (filterByGender) {
            users = users.stream()
                    .filter(nowUser -> !nowUser.getUserProfile().getGender().equals(userGender)) // 성별이 다를 때만 포함
                    .collect(Collectors.toList());
        }

        List<UserProfile> userProfiles = userProfileRepository.findByUserIn(users);

        return userProfiles.stream()
                .map(userProfile -> new ChatRoomDto.UserProfileDto(
                        userProfile.getUser().getName(),
                        userProfile.getEmoji() //이모지
                ))
                .collect(Collectors.toList());
    }

    //채팅방에 있는 사용자 조회
    @Transactional
    public List<ChatRoomDto.chatRoomUserList> getUserByRoomId(Long userId, Long roomId) {
        // 사용자가 해당 채팅방에 존재하는지 확인
        if (!joinChatRepository.existsByUserIdAndChatRoomId(userId, roomId)) {
            throw new BusinessException(Code.JOINCHAT_NOT_FOUND);
        }

        // 채팅방에 속한 사용자 프로필 가져오기
        List<ChatRoomDto.UserProfileDto> userProfileDtos = getUserProfilesByChatRoomId(userId, roomId, false);

        // 해당 채팅방의 모든 팀 조회
        List<TeamChatRoom> teamChatRooms = teamChatRoomRepository.findByChatRoomId(roomId);
        List<Long> teamIds = teamChatRooms.stream()
                .map(teamChatRoom -> teamChatRoom.getTeam().getId())
                .collect(Collectors.toList());

        // 한 번의 쿼리로 해당 채팅방의 모든 팀 사용자 관계 조회 (DB 조회 1회)
        List<UserTeam> userTeams = userTeamRepository.findByTeamIdIn(teamIds);

        // 사용자 목록을 Map으로 변환 (name을 key로 사용)
        Map<String, ChatRoomDto.UserProfileDto> userProfileMap = userProfileDtos.stream()
                .collect(Collectors.toMap(ChatRoomDto.UserProfileDto::getName, Function.identity()));

        // 팀별 사용자 매핑을 위한 Map 생성 (name 기반)
        Map<Long, List<String>> teamUserMap = userTeams.stream()
                .collect(Collectors.groupingBy(
                        userTeam -> userTeam.getTeam().getId(),
                        Collectors.mapping(userTeam -> userTeam.getUser().getName(), Collectors.toList())
                ));

        // 팀별 사용자 매핑
        List<ChatRoomDto.chatRoomUserList> teamUserLists = new ArrayList<>();

        for (TeamChatRoom teamChatRoom : teamChatRooms) {
            Long teamId = teamChatRoom.getTeam().getId();
            String teamName = teamChatRoom.getName();

            // 현재 팀에 속한 사용자 name 목록 가져오기
            List<String> teamUserNames = teamUserMap.getOrDefault(teamId, Collections.emptyList());

            // 사용자 목록을 name 기준으로 필터링
            List<ChatRoomDto.UserProfileDto> teamUsers = teamUserNames.stream()
                    .map(userProfileMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 팀별 사용자 목록 추가
            teamUserLists.add(ChatRoomDto.chatRoomUserList.builder()
                    .teamId(teamId)
                    .teamName(teamName)
                    .userProfiles(teamUsers)
                    .build());
        }

        return teamUserLists;
    }
}
