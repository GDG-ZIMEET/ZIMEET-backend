package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.meeting.service.RandomCommandService;
import com.gdg.z_meet.domain.meeting.service.RandomQueryService;
import com.gdg.z_meet.global.common.AuthenticatedUserUtils;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/random")
@Tag(name = "RandomMeeting")
@Validated
@Slf4j
public class RandomController {

    private final JwtUtil jwtUtil;
    private final RandomCommandService randomCommandService;
    private final RandomQueryService randomQueryService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "남은 티켓 개수")
    @GetMapping("/ticket")
    public Response<RandomResponseDTO.GetTicketDTO> getTicket() {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        RandomResponseDTO.GetTicketDTO response = randomQueryService.getTicket(userId);

        return Response.ok(response);
    }

    @Operation(summary = "랜덤 매칭")
    @MessageMapping("/matching/join")
    public Response<RandomResponseDTO.MatchingDTO> joinMatching(@Header("Authorization") String token) {

        Long userId = jwtUtil.extractUserIdFromToken(token);
        try {
            RandomResponseDTO.MatchingDTO response = randomCommandService.joinMatching(userId);
            return Response.ok(response);
        } catch (BusinessException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", ex.getReason().getStatus());
            errorResponse.put("code", ex.getCode());
            errorResponse.put("message", ex.getMessage());

            log.info("errorResponse: {}", errorResponse);
            return Response.fail(ex.getCode());
        }
    }

    @Operation(summary = "랜덤 매칭")
    @MessageMapping("/matching/cancel")
    public void cancelMatching(@Header("Authorization") String token) {

        Long userId = jwtUtil.extractUserIdFromToken(token);
        try {
            randomCommandService.cancelMatching(userId);
        } catch (BusinessException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", ex.getReason().getStatus());
            errorResponse.put("code", ex.getCode());
            errorResponse.put("message", ex.getMessage());

            log.info("errorResponse: {}", errorResponse);
        }
    }
}