package com.gdg.z_meet.domain.booth.controller;

import com.gdg.z_meet.domain.booth.converter.ClubConverter;
import com.gdg.z_meet.domain.booth.dto.ClubResponseDTO;
import com.gdg.z_meet.global.response.Response;
import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.service.ClubCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@Validated
public class ClubController {

    private final ClubCommandService clubCommandService;

    @PostMapping
    public Response<ClubResponseDTO.ClubCreateDTO> scheduleCreate(@RequestBody @Valid ClubRequestDTO.ClubCreateDTO request) {
        Club club = clubCommandService.createClub(request);
        return Response.ok(ClubConverter.toClubCreateDTO(club));
    }
}