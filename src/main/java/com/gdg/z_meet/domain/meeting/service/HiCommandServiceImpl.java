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
import java.util.function.Function;

@Service
@AllArgsConstructor
public class HiCommandServiceImpl implements HiCommandService {

    private final HiRepository hiRepository;
    private final TeamRepository teamRepository;
    private final MeetingQueryServiceImpl meetingQueryService;
    private final UserTeamRepository userTeamRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 중복 제거용 유틸 메서드 - Team/User 공통 처리
    public <T> Map<String, T> assignEntities(List<T> entities, Long fromId, Function<T, Long> idExtractor) {
        if (entities.size() != 2) throw new BusinessException(Code.ENTITY_NOT_FOUND);

        T from, to;
        if (idExtractor.apply(entities.get(0)).equals(fromId)) {
            from = entities.get(0);
            to = entities.get(1);
        } else {
            from = entities.get(1);
            to = entities.get(0);
        }

        Map<String, T> result = new HashMap<>();
        result.put("from", from);
        result.put("to", to);
        return result;
    }

    // 하이 중복 체크 로직 추출
    private void validateHiDuplication(Long fromId, Long toId, HiType hiType) {
        boolean exists = hiRepository.existsByFromIdAndToIdAndHiStatusNotAndHiType(fromId, toId, HiStatus.EXPIRED, hiType);
        if (exists) throw new BusinessException(Code.HI_DUPLICATION);
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
        Map<String, Team> teams = assignEntities(
                teamRepository.findByIdIn(teamIds),
                hiDto.getFromId(),
                Team::getId
        );

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
        Map<String, User> users = assignEntities(
                userRepository.findByIdIn(userIds),
                hiDto.getFromId(),
                User::getId
        );

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
        Long fromId = hiDto.getFromId();
        Long toId = hiDto.getToId();
        HiType type = hiDto.getType();

        Hi hi = hiRepository.findByFromIdAndToIdAndHiType(fromId, toId, type).orElseThrow(() -> new BusinessException(Code.HI_NOT_FOUND));

        if (hi == null) throw new BusinessException(Code.HI_NOT_FOUND);
        hi.changeStatus(HiStatus.REFUSE);
        hiRepository.save(hi);
    }
}
