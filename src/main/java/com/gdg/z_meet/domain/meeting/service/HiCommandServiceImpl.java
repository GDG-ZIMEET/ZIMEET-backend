package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.enums.HiStatus;
import com.gdg.z_meet.domain.meeting.entity.enums.HiType;
import com.gdg.z_meet.domain.meeting.repository.HiRepository;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class HiCommandServiceImpl implements HiCommandService {

    private final HiRepository hiRepository;
    private final TeamRepository teamRepository;
    private final MeetingQueryServiceImpl meetingQueryService;
    private final UserTeamRepository userTeamRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 팀 분리
    public Map<String, Team> assignTeams(List<Long> teamIds, Long fromId) {
        List<Team> teams = teamRepository.findByIdIn(teamIds);

        if (teams.size() != 2) {
            throw new BusinessException(Code.TEAM_NOT_FOUND); // 모든 팀을 못 찾은 경우
        }

        Team from = null;
        Team to = null;

        // 팀 순서 확인 후 할당
        if (teams.get(0).getId().equals(fromId)) {
            from = teams.get(0); // 보내는 팀
            to = teams.get(1);   // 받는 팀
        } else {
            from = teams.get(1);
            to = teams.get(0);
        }

        Map<String, Team> teamMap = new HashMap<>();
        teamMap.put("from", from);
        teamMap.put("to", to);
        return teamMap;
    }

    // 유저 분리
    public Map<String, User> assignUsers(List<Long> userIds, Long fromId) {
        List<User> users = userRepository.findByIdIn(userIds);

        if (users.size() != 2) {
            throw new BusinessException(Code.USER_NOT_FOUND); // 모든 팀을 못 찾은 경우
        }

        User from = null;
        User to = null;

        // 팀 순서 확인 후 할당
        if (users.get(0).getId().equals(fromId)) {
            from = users.get(0); // 보낸 유저
            to = users.get(1);   // 받은 유저
        } else {
            from = users.get(1);
            to = users.get(0);
        }

        Map<String, User> userMap = new HashMap<>();
        userMap.put("from", from);
        userMap.put("to", to);
        return userMap;
    }

    @Override
    public void sendHi(MeetingRequestDTO.hiDto hiDto) {
        if(hiDto.getType()== HiType.USER){
            sendUserHi(hiDto);
        }
        else sendTeamHi(hiDto);
    }


    @Override
    @Transactional
    public void sendTeamHi(MeetingRequestDTO.hiDto hiDto) {
        List<Long> teamIds = Arrays.asList(hiDto.getFromId(), hiDto.getToId());

        // 공통 메서드 호출하여 from, to 팀 할당
        Map<String, Team> teams = assignTeams(teamIds, hiDto.getFromId());
        Team from = teams.get("from");
        Team to = teams.get("to");

        // 유효성 검사
        if (from.getTeamType() != to.getTeamType()) throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        if (from.getGender() == to.getGender()) throw new BusinessException(Code.SAME_GENDER);
        if (hiRepository.existsByFromIdAndToIdAndHiStatusNotAndHiType(from.getId(), to.getId(), HiStatus.EXPIRED, HiType.TEAM)) {
            throw new BusinessException(Code.HI_DUPLICATION);
        }

        // 늘품제용 하이 무제한 설정
        //from.decreaseHi(); // 하이 갯수 차감

        Hi hi = Hi.builder()
                .hiStatus(HiStatus.NONE)
                .fromId(from.getId())
                .toId(to.getId())
                .hiType(HiType.TEAM)
                .build();
        hiRepository.save(hi);
    }

    @Override
    @Transactional
    public void sendUserHi(MeetingRequestDTO.hiDto hiDto) {
        List<Long> userIds = Arrays.asList(hiDto.getFromId(), hiDto.getToId());

        // 공통 메서드 호출하여 from, to 팀 할당
        Map<String, User> users = assignUsers(userIds, hiDto.getFromId());
        User from = users.get("from");
        User to = users.get("to");

        // 유효성 검사
        if (from.getUserProfile().getGender() == to.getUserProfile().getGender()) throw new BusinessException(Code.SAME_GENDER);
        if (hiRepository.existsByFromIdAndToIdAndHiStatusNotAndHiType(from.getId(), to.getId(), HiStatus.EXPIRED, HiType.USER)) {
            throw new BusinessException(Code.HI_DUPLICATION);
        }

        // 늘품제용 하이 무제한 설정
        //from.decreaseHi(); // 하이 갯수 차감

        Hi hi = Hi.builder()
                .hiStatus(HiStatus.NONE)
                .fromId(from.getId())
                .toId(to.getId())
                .hiType(HiType.USER)
                .build();
        hiRepository.save(hi);
    }

    @Override
    @Transactional
    public void refuseHi(MeetingRequestDTO.hiDto hiDto) {
        List<Long> teamIds = Arrays.asList(hiDto.getFromId(), hiDto.getToId());

        // 공통 메서드 호출하여 from, to 팀 할당
        Map<String, Team> teams = assignTeams(teamIds, hiDto.getFromId());
        Team from = teams.get("from");
        Team to = teams.get("to");

        Hi hi = hiRepository.findByFromIdAndToId(from.getId(), to.getId());
        if (hi == null) throw new BusinessException(Code.HI_NOT_FOUND);
        hi.changeStatus(HiStatus.REFUSE);
        hiRepository.save(hi);
    }
}
