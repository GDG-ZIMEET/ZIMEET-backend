package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MeetingCommandServiceImpl implements MeetingCommandService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public void createTeam(Long userId, TeamType teamType, MeetingRequestDTO.CreateTeamDTO request) {

        Set<Long> memberIds = new HashSet<>(request.getTeamMember());
        memberIds.add(userId);

        // 닉네임 중복 확인
        if (teamRepository.existsByName(request.getName())) {
            throw new BusinessException(Code.NAME_ALREADY_EXIST);
        }

        // 팀원 수 확인
        int userCount = memberIds.size();
        if (teamType == TeamType.TWO_TO_TWO && userCount != 2) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        } else if (teamType == TeamType.THREE_TO_THREE && userCount != 3) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }

        // 팀 존재하는지 확인
        if (teamRepository.existsAnyMemberByTeamType(new ArrayList<>(memberIds), teamType)) {
            throw new BusinessException(Code.TEAM_ALREADY_EXIST);
        }

        // 성별 확인
        List<User> teamMembers = userRepository.findAllByIdWithProfile(new ArrayList<>(memberIds));
        Gender gender = userProfileRepository.findByUserId(userId).get().getGender();
        if (teamMembers.stream()
                .anyMatch(user -> user.getUserProfile().getGender() != gender)) {
            throw new BusinessException(Code.TEAM_GENDER_MISMATCH);
        }

        Team newTeam = Team.builder()
                .teamType(teamType)
                .name(request.getName())
                .gender(gender)
                .build();
        teamRepository.save(newTeam);
        teamMembers.forEach(user -> MeetingConverter.toUserTeam(user, newTeam));
    }
}