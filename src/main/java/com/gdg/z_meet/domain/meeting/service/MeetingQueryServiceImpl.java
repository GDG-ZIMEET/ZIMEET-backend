package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.repository.MeetingRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingQueryServiceImpl implements MeetingQueryService {

    private final MeetingRepository meetingRepository;
    private final UserTeamRepository userTeamRepository;

    @Override
    public Team getTeam(Long teamId) {

        return meetingRepository.findById(teamId).orElseThrow(() -> new BusinessException(Code.TEAM_NOT_FOUND));
    }

    @Override
    public List<User> getUserTeam(Long teamId) {
        List<UserTeam> userTeams = userTeamRepository.findByTeamId(teamId);
        return userTeams.stream()
                .map(UserTeam::getUser)
                .collect(Collectors.toList());
    }
}