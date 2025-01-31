package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingQueryServiceImpl implements MeetingQueryService {

    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;

    @Override
    @Transactional(readOnly = true)
    public Team getTeam(Long userId, Long teamId) {

        if (userTeamRepository.existsByUserIdAndTeamId(userId, teamId)) {
            throw new BusinessException(Code.INVALID_MY_TEAM_ACCESS);
        }
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));
        validateTeamType(teamId, team.getTeamType());
        return team;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUserTeam(Long teamId) {

        List<UserTeam> userTeams = userTeamRepository.findByTeamId(teamId);
        return userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());
    }

    private void validateTeamType(Long teamId, TeamType teamType) {

        Integer userCount = userTeamRepository.countByTeamId(teamId);
        if (teamType != TeamType.TWO_TO_TWO && userCount == 2) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
        if (teamType != TeamType.THREE_TO_THREE && userCount == 3) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }
    }
}