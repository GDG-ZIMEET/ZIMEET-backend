package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Hi;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.meeting.entity.enums.HiStatus;
import com.gdg.z_meet.domain.meeting.repository.HiRepository;
import com.gdg.z_meet.domain.meeting.repository.TeamRepository;
import com.gdg.z_meet.domain.meeting.repository.UserTeamRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.UserProfile;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HiQueryServiceImpl implements HiQueryService{

    private final HiRepository hiRepository;
    private final TeamRepository teamRepository;
    private final MeetingQueryServiceImpl meetingQueryService;
    private final UserTeamRepository userTeamRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 공통 메서드로 분리
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
            from = teams.get(1); // 보내는 팀
            to = teams.get(0);   // 받는 팀
        }

        Map<String, Team> teamMap = new HashMap<>();
        teamMap.put("from", from);
        teamMap.put("to", to);
        return teamMap;
    }

    @Override
    @Transactional
    public void sendHi(MeetingRequestDTO.hiDto hiDto) {
        List<Long> teamIds = Arrays.asList(hiDto.getFromId(), hiDto.getToId());

        // 공통 메서드 호출하여 from, to 팀 할당
        Map<String, Team> teams = assignTeams(teamIds, hiDto.getFromId());
        Team from = teams.get("from");
        Team to = teams.get("to");

        // 유효성 검사
        if (from.getTeamType() != to.getTeamType()) throw new BusinessException(Code.TEAM_TYPE_MISMATCH);
        if (from.getGender() == to.getGender()) throw new BusinessException(Code.SAME_GENDER);
        if (hiRepository.existsByFromAndToAndHiStatusNot(from, to, HiStatus.EXPIRED)) {
            throw new BusinessException(Code.HI_DUPLICATION);
        }

        // 늘품제용 하이 무제한 설정
        //from.decreaseHi(); // 하이 갯수 차감

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

        // 공통 메서드 호출하여 from, to 팀 할당
        Map<String, Team> teams = assignTeams(teamIds, hiDto.getFromId());
        Team from = teams.get("from");
        Team to = teams.get("to");

        Hi hi = hiRepository.findByFromAndTo(from, to);
        if (hi == null) throw new BusinessException(Code.HI_NOT_FOUND);
        hi.changeStatus(HiStatus.REFUSE);
        hiRepository.save(hi);
    }

    @Override
    @Transactional
    public List<MeetingResponseDTO.hiListDto> checkHiList(Long userId, String action){
        List<UserTeam> myTeams = userTeamRepository.findByUserId(userId);
        if(myTeams.size()==0) throw new BusinessException(Code.TEAM_NOT_FOUND);
        List<Long> myTeamIds = myTeams.stream()
                .map(userTeam -> userTeam.getTeam().getId()) // UserTeam에서 teamId 추출
                .collect(Collectors.toList());

        List<Hi> hiList;
        List<Long> teamIds;
        if(action.equals("Receive")) {
            hiList = hiRepository.findRecevieHiList(myTeamIds);
            teamIds = hiList.stream()
                    .map(hi -> hi.getFrom().getId()) // 보낸 팀의 id
                    .collect(Collectors.toSet()) // 중복 제거
                    .stream().toList();
        }
        else{
            hiList = hiRepository.findSendHiList(myTeamIds);
            teamIds = hiList.stream()
                    .map(hi -> hi.getTo().getId())
                    .collect(Collectors.toSet())
                    .stream().toList();
        }

        List<Team> teamList = teamRepository.findByIdIn(teamIds);

        // 여러 개의 hiListDto 생성
        List<MeetingResponseDTO.hiListDto> hiListDtos = new ArrayList<>();
        for (Hi hi : hiList) {
            if(hi.getHiStatus()!=HiStatus.NONE && action.equals("Receive")) continue;

            Team team = action.equals("Receive") ? hi.getFrom() : hi.getTo();
            Team myteam = action.equals("Receive") ? hi.getTo() : hi.getFrom();

            // 각 팀에 대해 UserProfileDto 만들기
            List<UserProfile> userProfiles = userProfileRepository.findByTeamId(team.getId());

            // UserProfileDto 리스트 생성
            List<MeetingResponseDTO.hiListDto.UserProfileDto> userProfileDtos = new ArrayList<>();
            int sum = 0;
            // 반복문 돌면서 DTO 객체를 리스트에 추가
            for (UserProfile userProfile : userProfiles) {
                sum += userProfile.getAge();
                userProfileDtos.add(new MeetingResponseDTO.hiListDto.UserProfileDto(
                        userProfile.getMajor(),
                        userProfile.getEmoji(),
                        userProfile.getMusic()
                ));
            }

            double averageAge = userProfiles.isEmpty() ? 0 : Math.round((sum / (double) userProfiles.size()) * 10.0) / 10.0;


            LocalDateTime sentTime = hi.getCreatedAt(); // Hi 생성 시간
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            String dateTime = String.format("%s 전송", sentTime.format(formatter));

            if(action.equals("Receive")){
                LocalDateTime now = LocalDateTime.now(); // 현재 시간
                Duration duration = Duration.between(sentTime, now);

                long totalMinutesElapsed = duration.toMinutes(); // 보낸 후 총 경과된 분
                long totalMinutesRemaining = (5 * 60) - totalMinutesElapsed; // 5시간(300분) 기준으로 남은 분 계산

                if (totalMinutesRemaining <= 0) {
                    hi.changeStatus(HiStatus.EXPIRED);
                    hiRepository.save(hi);
                    continue;
                }

                long remainingHours = totalMinutesRemaining / 60; // 남은 시간을 60으로 나눠서 시간 계산
                long remainingMinutes = totalMinutesRemaining % 60; // 나머지 분 계산

                dateTime = String.format("%d시간 %d분 남음", remainingHours, remainingMinutes);

                if(remainingHours<=0 || remainingMinutes<0){
                    hi.changeStatus(HiStatus.EXPIRED);
                    hiRepository.save(hi);
                    continue;
                }
            }

            // 하나의 hiListDto 생성
            MeetingResponseDTO.hiListDto hiDto = MeetingResponseDTO.hiListDto.builder()
                    .myTeamId(myteam.getId())
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .userProfileDtos(userProfileDtos)
                    .age(averageAge)
                    .dateTime(dateTime)
                    .build();

            hiListDtos.add(hiDto); // hiListDto를 리스트에 추가
        }

        return hiListDtos;
    }
}
