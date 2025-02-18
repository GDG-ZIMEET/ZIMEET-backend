package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.meeting.service.RandomQueryService;
import com.gdg.z_meet.global.common.AuthenticatedUserUtils;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/random")
@Tag(name = "RandomMeeting")
@Validated
public class RandomController {

    private final RandomQueryService randomQueryService;

    @Operation(summary = "남은 티켓 개수")
    @GetMapping("/ticket")
    public Response<RandomResponseDTO.GetTicketDTO> getTicket() {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        RandomResponseDTO.GetTicketDTO response = randomQueryService.getTicket(userId);

        return Response.ok(response);
    }
}