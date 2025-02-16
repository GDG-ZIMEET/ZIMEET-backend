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
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

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

        Map<Long, List<String>> emojiList = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
                    return userTeams.stream()
                            .map(userTeam -> userTeam.getUser().getUserProfile().getEmoji())
                            .collect(Collectors.toList());
                }
        ));

        Map<Long, List<String>> majorList = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
                    return userTeams.stream()
                            .map(userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMajor()))
                            .distinct()
                            .collect(Collectors.toList());
                }
        ));

        Map<Long, Double> age = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> userTeamRepository.findByTeamId(team.getId()).stream()
                        .mapToInt(userTeam -> userTeam.getUser().getUserProfile().getAge())
                        .average()
                        .orElse(0.0)
        ));

        Map<Long, List<String>> musicList = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
                    return userTeams.stream()
                            .map(userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMusic()))
                            .distinct()
                            .collect(Collectors.toList());
                }
        ));

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

        Team team = teamRepository.findByTeamType(userId, teamType)
                .orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));

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

    private void validateTeamType(Long teamId, TeamType teamType) {

        Long userCount = userTeamRepository.countByTeamId(teamId);
        if (teamType != TeamType.TWO_TO_TWO && userCount == 2) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
        if (teamType != TeamType.THREE_TO_THREE && userCount == 3) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponseDTO.hiListDto> receiveHiList(Long teamId) {
        List<Hi> hiList = hiRepository.findRecevieHiList(teamId);
        List<Long> teamIds = hiList.stream()
                .map(hi -> hi.getFrom().getId()) // 보낸 팀의 id
                .collect(Collectors.toList());

        List<Team> teamList = teamRepository.findByIdIn(teamIds);

        Map<Long, List<String>> emojiList = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
                    return userTeams.stream()
                            .map(userTeam -> userTeam.getUser().getUserProfile().getEmoji())
                            .collect(Collectors.toList());
                }
        ));

        Map<Long, List<String>> majorList = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
                    return userTeams.stream()
                            .map(userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMajor()))
                            .distinct()
                            .collect(Collectors.toList());
                }
        ));

        Map<Long, Double> age = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> userTeamRepository.findByTeamId(team.getId()).stream()
                        .mapToInt(userTeam -> userTeam.getUser().getUserProfile().getAge())
                        .average()
                        .orElse(0.0)
        ));

        Map<Long, List<String>> musicList = teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> {
                    List<UserTeam> userTeams = userTeamRepository.findByTeamId(team.getId());
                    return userTeams.stream()
                            .map(userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMusic()))
                            .distinct()
                            .collect(Collectors.toList());
                }
        ));

        // 여러 개의 hiListDto 생성
        List<MeetingResponseDTO.hiListDto> hiListDtos = new ArrayList<>();
        for (Hi hi : hiList) {
            if(hi.getHiStatus()!=HiStatus.NONE) continue;
            // 각 팀에 대해 UserProfileDto 만들기
            List<MeetingResponseDTO.hiListDto.UserProfileDto> userProfileDtos = new ArrayList<>();
            Team team = hi.getFrom(); // 보내는 팀
            MeetingResponseDTO.hiListDto.UserProfileDto userProfileDto = MeetingResponseDTO.hiListDto.UserProfileDto.builder()
                    .major(String.join(", ", majorList.get(team.getId())))
                    .emoji(String.join(", ", emojiList.get(team.getId())))
                    .music(String.join(", ", musicList.get(team.getId())))
                    .build();

            userProfileDtos.add(userProfileDto);

            LocalDateTime sentTime = hi.getCreatedAt(); // Hi 생성 시간
            LocalDateTime now = LocalDateTime.now(); // 현재 시간

            Duration duration = Duration.between(sentTime, now);
            long remainingHours = 5 - duration.toHours(); // 남은 시간 계산 (5시간 기준)
            long remainingMinutes = 60 - duration.toMinutesPart(); // 남은 분 계산

            String remainingTime = String.format("%d시간 %d분 남음", remainingHours, remainingMinutes);

            if(remainingHours==0 && remainingMinutes==0){
                hi.changeStatus(HiStatus.REFUSE);
                continue;
            }

            // 하나의 hiListDto 생성
            MeetingResponseDTO.hiListDto hiDto = MeetingResponseDTO.hiListDto.builder()
                    .teamName(team.getName())
                    .teamList(userProfileDtos)
                    .age(Math.round(age.get(team.getId()) * 10.0) / 10.0)
                    .dateTime(remainingTime) // 남은 시간 추가
                    .build();

            hiListDtos.add(hiDto); // hiListDto를 리스트에 추가
        }

        return hiListDtos;
    }

}