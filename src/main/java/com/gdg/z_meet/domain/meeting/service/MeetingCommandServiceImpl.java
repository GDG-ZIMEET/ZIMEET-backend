package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingCommandServiceImpl implements MeetingCommandService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public void createTeam(Long userId, TeamType teamType, MeetingRequestDTO.CreateTeamDTO request) {

        int userCount = request.getTeamMember().size();
        if (teamType == TeamType.TWO_TO_TWO && userCount != 2) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        } else if (teamType == TeamType.THREE_TO_THREE && userCount != 3) {
            throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        }

        Gender gender = userProfileRepository.findById(userId).get().getGender();
        Team newTeam = Team.builder()
                        .teamType(teamType)
                        .name(request.getName())
                        .gender(gender)
                        .build();
        teamRepository.save(newTeam);

        for(Long teamMemberId : request.getTeamMember()) {

            User user = userRepository.findById(teamMemberId).get();
            if (user.getUserProfile().getGender() != gender) {
                throw new BusinessException(Code.TEAM_GENDER_MISMATCH);
            }
            MeetingConverter.toCreateUserTeam(user, newTeam);
        }
    }
}