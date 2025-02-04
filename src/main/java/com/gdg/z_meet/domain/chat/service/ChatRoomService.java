package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.*;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
import com.gdg.z_meet.domain.chat.repository.TeamChatRoomRepository;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.UserProfile;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.h2.api.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserTeamRepository userTeamRepository;
    private final TeamRepository teamRepository;
    private final TeamChatRoomRepository teamChatRoomRepository;

    private static final String CHAT_ROOMS_KEY = "chatrooms";
    private static final String CHAT_ROOM_ACTIVITY_KEY = "chatroom:activity";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";

    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom() {
        // 1. 새로운 ChatRoom 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .build();

        // 2. DB 저장
        chatRoom = chatRoomRepository.save(chatRoom);

        // 3. Redis 저장
        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, chatRoom.getId().toString(), chatRoom);

        return chatRoom;
    }

    // 채팅방 삭제
    @Transactional
    public void deleteChatRoom(Long chatRoomId) {
        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));

        // 연관된 메시지 삭제
        List<Message> messages = messageRepository.findByChatRoomId(chatRoomId);
        if (!messages.isEmpty()) {
            messageRepository.deleteAll(messages);
        }

        // 연관된 JoinChat 삭제
        List<JoinChat> joinChats = joinChatRepository.findByChatRoomId(chatRoomId);
        if (!joinChats.isEmpty()) {
            joinChats.forEach(joinChat -> {
                String joinChatsKey = "user:" + joinChat.getUser().getId() + ":chatrooms";
                redisTemplate.opsForSet().remove(joinChatsKey, chatRoomId);
            });
            joinChatRepository.deleteAll(joinChats);
        }

        // Redis에서 채팅방 삭제
        redisTemplate.opsForHash().delete(CHAT_ROOMS_KEY, chatRoomId.toString());

        // Redis의 활동 시간 데이터 삭제
        redisTemplate.opsForZSet().remove(CHAT_ROOM_ACTIVITY_KEY, chatRoomId.toString());

        // 채팅방 삭제
        chatRoomRepository.deleteById(chatRoomId);

    }

    // 사용자 추가
    @Transactional
    public ChatRoomDto.resultChatRoomDto addTeamJoinChat(ChatRoomDto.TeamListDto teamListDto) {

        //채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder().build();
        chatRoom = chatRoomRepository.save(chatRoom);

        Team team1 = teamRepository.findById(teamListDto.getTeamId1()).orElseThrow(()-> new BusinessException(Code.TEAM_NOT_FOUND));
        Team team2 = teamRepository.findById(teamListDto.getTeamId2()).orElseThrow(()-> new BusinessException(Code.TEAM_NOT_FOUND));

        addTeamToChatRoom(chatRoom, team1, team2.getName());
        addTeamToChatRoom(chatRoom, team2, team1.getName());

        return ChatRoomDto.resultChatRoomDto.builder()
                .chatRoomid(chatRoom.getId())
                .build();
    }

    @Transactional
    public void addTeamToChatRoom(ChatRoom chatRoom, Team team, String teamName){
        Long chatRoomId = chatRoom.getId();

        List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
        List<User> users = userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());

        // 팀정보 DB 저장
        TeamChatRoom teamChatRoom = TeamChatRoom.builder()
                .team(team)
                .chatRoom(chatRoom)
                .name(teamName)
                .build();

        teamChatRoomRepository.save(teamChatRoom);

        for (User user : users){
            Long userId = user.getId();

            // 이미 존재하는 경우 에러 발생
            boolean isAlreadyExists = joinChatRepository.findByUserAndChatRoom(user, chatRoom).isPresent();
            if (isAlreadyExists) {
                throw new BusinessException(Code.JOINCHAT_ALREADY_EXIST);
            }
            //User 정보 DB 저장
            JoinChat joinChat = JoinChat.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .build();

            joinChatRepository.save(joinChat);

            // 사용자 -> 참여 채팅방 매핑 데이터 저장
            String joinChatsKey = "user:" + userId + ":chatrooms";
            redisTemplate.opsForSet().add(joinChatsKey, chatRoomId);

            // 채팅방 -> 참여 사용자 매핑 데이터 저장
            String chatRoomUsersKey = "chatroom:" + chatRoomId + ":users";
            redisTemplate.opsForSet().add(chatRoomUsersKey, userId);

        }
    }

    // 사용자 제거
    @Transactional
    public void removeUserFromChatRoom(Long chatRoomId, Long userId) {

        // DB에서 제거
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
        ChatRoom chatRoom = getChatRoomById(chatRoomId);

        JoinChat joinChat = joinChatRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() ->  new BusinessException(Code.JOINCHAT_NOT_FOUND));

        joinChatRepository.delete(joinChat);


        String joinChatsKey = "user:" + userId + ":chatrooms";
        String chatRoomUsersKey = "chatroom:" + chatRoomId + ":users";

        // 사용자와 채팅방 간 매핑 데이터 제거 (Redis)
        redisTemplate.opsForSet().remove(joinChatsKey, chatRoomId);
        redisTemplate.opsForSet().remove(chatRoomUsersKey, userId);

    }


    // 채팅방 ID로 채팅방 정보 조회
    private ChatRoom getChatRoomById(Long chatRoomId) {
        Object chatRoomObj = redisTemplate.opsForHash().get("chatrooms", chatRoomId.toString());
        if (chatRoomObj instanceof ChatRoom) {
            return (ChatRoom) chatRoomObj;
        }
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));
        redisTemplate.opsForHash().put("chatrooms", chatRoomId.toString(), chatRoom);
        return chatRoom;
    }

    // 사용자 채팅방 목록 (최신 활동 기준 정렬)
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
            List<JoinChat> joinChats = joinChatRepository.findByUserId(userId);
            joinChatIdsSet = joinChats.stream()
                    .map(joinChat -> joinChat.getChatRoom().getId())
                    .collect(Collectors.toSet());
            joinChatIdsSet.forEach(chatRoomId -> redisTemplate.opsForSet().add(joinChatsKey, chatRoomId));
        }


        // Redis ZSet에서 최신 활동 기준으로 정렬된 채팅방 ID 가져오기
        List<Long> sortedChatRoomIds = redisTemplate.opsForZSet()
                .reverseRange(CHAT_ROOM_ACTIVITY_KEY, 0, -1)
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

                    List<ChatRoomDto.UserProfileDto> userProfiles = getUserProfilesByChatRoomId(chatRoomId);


                    return new ChatRoomDto.chatRoomListDto(chatRoom.getId(), chatRoomName, latestMessage, latestMessageTime, userProfiles);
                })
                .collect(Collectors.toList());
    }

    //상대팀 이름 반환
    @Transactional
    public String getChatRoomName(Long userId, Long chatRoomId){
        // 해당 채팅방의 팀 조회
        List<TeamChatRoom> teamChatRooms = teamChatRoomRepository.findByChatRoomId(chatRoomId);

        // 현재 사용자가 속한 팀 찾기
        TeamChatRoom userTeamChatRoom = teamChatRooms.stream()
                .filter(teamChatRoom ->
                        userTeamRepository.existsByUserIdAndTeamId(userId, teamChatRoom.getTeam().getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(Code.TEAM_USER_NOT_FOUND));

        // 상대팀 이름 리턴
        return userTeamChatRoom.getName();
    }

    @Transactional
    public List<ChatRoomDto.UserProfileDto> getUserProfilesByChatRoomId(Long chatRoomId) {
        List<User> users = joinChatRepository.findUsersByChatRoomId(chatRoomId);
        List<UserProfile> userProfiles = userProfileRepository.findByUserIn(users);

        return userProfiles.stream()
                .map(userProfile -> new ChatRoomDto.UserProfileDto(
                        userProfile.getUser().getId(),
                        userProfile.getUser().getName(),
                        userProfile.getEmoji() //이모지
                ))
                .collect(Collectors.toList());
    }

    //채팅방에 있는 사용자 조회
    @Transactional
    public List<ChatRoomDto.UserProfileDto> getUserByRoomId(Long userId, Long roomId){
        if (!joinChatRepository.existsByUserIdAndChatRoomId(userId, roomId))
            throw new BusinessException(Code.JOINCHAT_NOT_FOUND);

        return getUserProfilesByChatRoomId(roomId);
    }


}
