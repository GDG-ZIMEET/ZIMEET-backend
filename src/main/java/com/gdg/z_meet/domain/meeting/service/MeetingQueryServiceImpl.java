package com.gdg.z_meet.domain.meeting.service;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        Team from = teams.get(0);  //보내는 팀
        Team to = teams.get(1);    //받는 팀

        validateTeamType(from.getId(), from.getTeamType());
        validateTeamType(to.getId(), to.getTeamType());

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

    private void validateTeamType(Long teamId, TeamType teamType) {

        Long userCount = userTeamRepository.countByTeamId(teamId);
        if (teamType != TeamType.TWO_TO_TWO && userCount == 2) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
        if (teamType != TeamType.THREE_TO_THREE && userCount == 3) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
    }
}