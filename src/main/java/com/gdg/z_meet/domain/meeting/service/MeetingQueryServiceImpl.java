package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.enums.ActiveStatus;
import com.gdg.z_meet.domain.meeting.entity.enums.Event;
import com.gdg.z_meet.domain.meeting.entity.enums.TeamType;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.repository.HiRepository;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final HiRepository hiRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final Event event = Event.NEUL_2025;

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetTeamGalleryDTO getTeamGallery(Long userId, TeamType teamType, Integer page) {

        Gender gender = userProfileRepository.findByUserId(userId).get().getGender();
        List<Team> teamList = teamRepository.findAllByTeamType(userId, gender, teamType, event, PageRequest.of(page, 12));
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

        if (userTeamRepository.existsByUserIdAndTeamIdAndActiveStatus(userId, teamId)) {
            throw new BusinessException(Code.INVALID_MY_TEAM_ACCESS);
        }
        Team team = teamRepository.findByIdAndEvent(teamId, event).orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));
        if (team.getActiveStatus() == ActiveStatus.INACTIVE) {
            throw new BusinessException(Code.TEAM_ALREADY_DELETED);
        }

        validateTeamType(teamId, team.getTeamType());

        List<UserTeam> userTeams = userTeamRepository.findByTeamIdAndActiveStatus(teamId);
        List<User> users = userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());

        Team myTeam = teamRepository.findByTeamType(userId, team.getTeamType(), event)
                .orElseThrow(() -> new BusinessException(Code.MY_TEAM_NOT_FOUND));
        Boolean hi = hiRepository.existsByFromAndTo(myTeam, team);

        return MeetingConverter.toGetTeamDTO(team, users, hi);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetPreMyTeamDTO getPreMyTeam(Long userId, TeamType teamType) {

        Optional<Team> teamOptional = teamRepository.findByTeamType(userId, teamType, event);
        if (teamOptional.isEmpty()) {
            return null;
        }

        Team team = teamOptional.get();
        validateTeamType(team.getId(), teamType);

        List<UserTeam> userTeams = userTeamRepository.findByTeamIdAndActiveStatus(team.getId());
        List<String> emojiList = userTeams.stream()
                            .map(userTeam -> userTeam.getUser().getUserProfile().getEmoji())
                            .collect(Collectors.toList());

        return MeetingConverter.toGetPreMyTeamDTO(team, emojiList);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetMyTeamDTO getMyTeam(Long userId, TeamType teamType) {

        Team team = teamRepository.findByTeamType(userId, teamType, event)
                .orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));

        validateTeamType(team.getId(), teamType);

        List<UserTeam> userTeams = userTeamRepository.findByTeamIdAndActiveStatus(team.getId());
        List<User> users = userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());

        return MeetingConverter.toGetMyTeamDTO(team, users);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetMyTeamHiDTO getMyTeamHi(Long userId, TeamType teamType) {

        Team team = teamRepository.findByTeamType(userId, teamType, event)
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
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetSearchListDTO getSearch(Long userId, TeamType teamType, String nickname, String phoneNumber) {

        if (nickname == null && phoneNumber == null) {
            throw new BusinessException(Code.SEARCH_FILTER_NULL);
        }
        if (nickname != null && phoneNumber != null) {
            throw new BusinessException(Code.SEARCH_FILTER_EXCEEDED);
        }

        Gender gender = userProfileRepository.findByUserId(userId).get().getGender();
        List<User> users;

        if (nickname != null) {
            users = userRepository.findAllByNicknameWithProfile(gender, nickname, userId, teamType);
        } else if (phoneNumber.length() >= 7) {
            users = userRepository.findAllByPhoneNumberWithProfile(gender, phoneNumber, userId, teamType);
        } else {
            users = Collections.emptyList();
        }
        return MeetingConverter.GetSearchListDTO(users);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponseDTO.GetMyDeleteDTO getMyDelete(Long userId) {

        User user = userRepository.findByIdWithProfile(userId);

        return MeetingConverter.toGetMyDeleteDTO(user);
    }

    private Map<Long, List<String>> collectEmoji(List<Team> teamList) {

        return collectTeamInfo(teamList,
                userTeam -> userTeam.getUser().getUserProfile().getEmoji(),
                false);
    }

    private Map<Long, List<String>> collectMajor(List<Team> teamList) {

        return collectTeamInfo(teamList,
                userTeam -> String.valueOf(userTeam.getUser().getUserProfile().getMajor().getShortName()),
                true);
    }

    private Map<Long, Double> collectAge(List<Team> teamList) {

        return teamList.stream().collect(Collectors.toMap(
                Team::getId, team -> userTeamRepository.findByTeamIdAndActiveStatus(team.getId()).stream()
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
                    Stream<String> stream = userTeamRepository.findByTeamIdAndActiveStatus(team.getId())
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


}