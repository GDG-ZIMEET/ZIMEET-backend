package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.chat.entity.TeamChatRoom;
import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.entity.status.HiStatus;
import com.gdg.z_meet.domain.meeting.repository.HiRepository;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MeetingQueryServiceImpl implements MeetingQueryService {

    private final UserProfileRepository userProfileRepository;
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final HiRepository hiRepository;

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetTeamGalleryDTO getTeamGallery(Long userId, TeamType teamType, Integer page) {

        Gender gender = userProfileRepository.findByUserId(userId).get().getGender();
        List<Team> teamList = teamRepository.findAllByTeamType(userId, gender, teamType, PageRequest.of(page, 12));
        Collections.shuffle(teamList);

        Map<Long, List<String>> emojiList = collectEmoji(teamList);
        Map<Long, List<String>> majorList = collectMajor(teamList);
        Map<Long, Double> age = collectAge(teamList);
        Map<Long, List<String>> musicList = collectMusic(teamList);

        return MeetingConverter.toGetTeamGalleryDTO(teamList, emojiList, majorList, age, musicList);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetTeamDTO getTeam(Long userId, Long teamId) {

        if (userTeamRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new BusinessException(Code.INVALID_MY_TEAM_ACCESS);
        }
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));
        validateTeamType(teamId, team.getTeamType());

        List<UserTeam> userTeams = userTeamRepository.findByTeamId(teamId);
        List<User> users = userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());

        return MeetingConverter.toGetTeamDTO(team, users);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetMyTeamDTO getPreMyTeam(Long userId, TeamType teamType) {

        Optional<Team> teamOptional = teamRepository.findByTeamType(userId, teamType);
        if (teamOptional.isEmpty()) {
            return null;
        }

        Team team = teamOptional.get();
        validateTeamType(team.getId(), teamType);

        List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
        List<String> emojiList = userTeams.stream()
                            .map(userTeam -> userTeam.getUser().getUserProfile().getEmoji())
                            .collect(Collectors.toList());

        return MeetingConverter.toGetMyTeamDTO(team, emojiList);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetTeamDTO getMyTeam(Long userId, TeamType teamType) {

        Team team = teamRepository.findByTeamType(userId, teamType)
                .orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));

        validateTeamType(team.getId(), teamType);

        List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
        List<User> users = userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());

        return MeetingConverter.toGetTeamDTO(team, users);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetMyTeamHiDTO getMyTeamHi(Long userId, TeamType teamType) {

        Team team = teamRepository.findByTeamType(userId, teamType)
                .orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));

        validateTeamType(team.getId(), teamType);

        return MeetingConverter.toGetMyTeamHiDTO(team);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.CheckNameDTO checkName(String name) {

        Boolean exist = teamRepository.existsByName(name);

        return MeetingResponseDTO.CheckNameDTO.builder()
                .check(exist == Boolean.TRUE ? 0 : 1)
                .build();
    }

    @Override
    @Transactional()
    public void sendHi(MeetingRequestDTO.hiDto hiDto){
        List<Long> teamIds = Arrays.asList(hiDto.getFromId(), hiDto.getToId());
        List<Team> teams = teamRepository.findByIdIn(teamIds);

        if (teams.size() != 2) {
            throw new BusinessException(Code.TEAM_NOT_FOUND); // 모든 팀을 못 찾은 경우
        }

        Team from = null;
        Team to = null;

        // 팀 순서 확인 후 할당
        if (teams.get(0).getId().equals(hiDto.getFromId())) {
            from = teams.get(0); // 보내는 팀
            to = teams.get(1);   // 받는 팀
        } else {
            from = teams.get(1); // 보내는 팀
            to = teams.get(0);   // 받는 팀
        }


        if(from.getTeamType()!=to.getTeamType()) throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        if(from.getGender()==to.getGender()) throw new BusinessException(Code.SAME_GENDER);
        if(hiRepository.existsByFromAndTo(from,to)) throw new BusinessException(Code.HI_DUPLICATION);

        from.decreaseHi(); // 하이 갯수 차감

        Hi hi = Hi.builder()
                .hiStatus(HiStatus.NONE)
                .from(from)
                .to(to)
                .build();
        hiRepository.save(hi);
    }

    @Override
    @Transactional
    public void refuseHi(MeetingRequestDTO.hiDto hiDto) {
        List<Long> teamIds = Arrays.asList(hiDto.getFromId(), hiDto.getToId());
        List<Team> teams = teamRepository.findByIdIn(teamIds);

        if (teams.size() != 2) {
            throw new BusinessException(Code.TEAM_NOT_FOUND); // 모든 팀을 못 찾은 경우
        }

        Team from = null;
        Team to = null;

        // 팀 순서 확인 후 할당
        if (teams.get(0).getId().equals(hiDto.getFromId())) {
            from = teams.get(0); // 보내는 팀
            to = teams.get(1);   // 받는 팀
        } else {
            from = teams.get(1); // 보내는 팀
            to = teams.get(0);   // 받는 팀
        }

        Hi hi = hiRepository.findByFromAndTo(from,to);
        hi.changeStatus(HiStatus.REFUSE);
    }

    private Map<Long, List<String>> collectEmoji(List<Team> teamList) {

        return collectTeamInfo(teamList,
                userTeam -> userTeam.getUser().getUserProfile().getEmoji(),
                false);
    }

    private Map<Long, List<String>> collectMajor(List<Team> teamList) {

        return collectTeamInfo(teamList,
                userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMajor()),
                true);
    }

    private Map<Long, Double> collectAge(List<Team> teamList) {

        return teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> userTeamRepository.findByTeamId(team.getId()).stream()
                        .mapToInt(userTeam -> userTeam.getUser().getUserProfile().getAge())
                        .average()
                        .orElse(0.0)
        ));
    }

    private Map<Long, List<String>> collectMusic(List<Team> teamList) {

        return collectTeamInfo(teamList,
                userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMusic()),
                true);
    }

    @Transactional(readOnly = true)
    protected Map<Long, List<String>> collectTeamInfo(List<Team> teamList,
                                                      Function<UserTeam, String> mapper,
                                                      boolean distinct) {

        return teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    Stream<String> stream = userTeamRepository.findByTeamId(team.getId())
                            .stream()
                            .map(mapper);
                    return (distinct ? stream.distinct() : stream)
                            .collect(Collectors.toList());
                }
        ));
    }

    @Transactional(readOnly = true)
    protected void validateTeamType(Long teamId, TeamType teamType) {

        Long userCount = userTeamRepository.countByTeamId(teamId);
        if (teamType == TeamType.TWO_TO_TWO && userCount != 2) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
        if (teamType == TeamType.THREE_TO_THREE && userCount != 3) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
    }

    @Override
    @Transactional
    public List<MeetingResponseDTO.hiListDto> checkHiList(Long teamId, String action){
        List<Hi> hiList;
        List<Long> teamIds;
        if(action.equals("Receive")) {
            hiList = hiRepository.findRecevieHiList(teamId);
            teamIds = hiList.stream()
                    .map(hi -> hi.getFrom().getId()) // 보낸 팀의 id
                    .collect(Collectors.toList());
        }
        else{
            hiList = hiRepository.findSendHiList(teamId);
            teamIds = hiList.stream()
                    .map(hi -> hi.getTo().getId())
                    .collect(Collectors.toList());
        }

        List<Team> teamList = teamRepository.findByIdIn(teamIds);
        System.out.println("TeamList: "+teamList);

        Map<Long, List<String>> emojiList = collectEmoji(teamList);
        Map<Long, List<String>> majorList = collectMajor(teamList);
        Map<Long, Double> age = collectAge(teamList);
        Map<Long, List<String>> musicList = collectMusic(teamList);


        // 여러 개의 hiListDto 생성
        List<MeetingResponseDTO.hiListDto> hiListDtos = new ArrayList<>();
        for (Hi hi : hiList) {
            if(hi.getHiStatus()!=HiStatus.NONE && action.equals("Receive")) continue;

            Team team = action.equals("Receive") ? hi.getFrom() : hi.getTo();

            // UserProfileDto 생성 (null 방지)
            String major = String.join(", ", majorList.getOrDefault(team.getId(), Collections.emptyList()));
            String emoji = String.join(", ", emojiList.getOrDefault(team.getId(), Collections.emptyList()));
            String music = String.join(", ", musicList.getOrDefault(team.getId(), Collections.emptyList()));

            // 각 팀에 대해 UserProfileDto 만들기
            List<MeetingResponseDTO.hiListDto.UserProfileDto> userProfileDtos = new ArrayList<>();
            MeetingResponseDTO.hiListDto.UserProfileDto userProfileDto = MeetingResponseDTO.hiListDto.UserProfileDto.builder()
                    .major(major)
                    .emoji(emoji)
                    .music(music)
                    .build();
            userProfileDtos.add(userProfileDto);

            LocalDateTime sentTime = hi.getCreatedAt(); // Hi 생성 시간
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            String dateTime = String.format("%s 전송", sentTime.format(formatter));

            if(action.equals("Receive")){
                LocalDateTime now = LocalDateTime.now(); // 현재 시간
                Duration duration = Duration.between(sentTime, now);

                long totalMinutesElapsed = duration.toMinutes(); // 보낸 후 총 경과된 분
                long totalMinutesRemaining = (5 * 60) - totalMinutesElapsed; // 5시간(300분) 기준으로 남은 분 계산

                if (totalMinutesRemaining <= 0) {
                    hi.changeStatus(HiStatus.REFUSE);
                    hiRepository.save(hi);
                    continue;
                }

                long remainingHours = totalMinutesRemaining / 60; // 남은 시간을 60으로 나눠서 시간 계산
                long remainingMinutes = totalMinutesRemaining % 60; // 나머지 분 계산

                dateTime = String.format("%d시간 %d분 남음", remainingHours, remainingMinutes);

               if(remainingHours<=0 || remainingMinutes<=0){
                   hi.changeStatus(HiStatus.REFUSE);
                   hiRepository.save(hi);
                   continue;
               }
           }

            // 하나의 hiListDto 생성
            MeetingResponseDTO.hiListDto hiDto = MeetingResponseDTO.hiListDto.builder()
                    .teamName(team.getName())
                    .teamList(userProfileDtos)
                    .age(Math.round(age.get(team.getId()) * 10.0) / 10.0)
                    .dateTime(dateTime)
                    .build();

            hiListDtos.add(hiDto); // hiListDto를 리스트에 추가
        }

        return hiListDtos;
    }


}