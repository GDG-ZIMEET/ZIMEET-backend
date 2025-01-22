package com.gdg.z_meet.domain.booth.controller;

import com.gdg.z_meet.domain.booth.converter.ClubConverter;
import com.gdg.z_meet.domain.booth.dto.ClubResponseDTO;
import com.gdg.z_meet.domain.booth.service.ClubQueryService;
import com.gdg.z_meet.global.response.Response;
import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.service.ClubCommandService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/booths")
@Validated
public class ClubController {

    private final ClubCommandService clubCommandService;
    private final ClubQueryService clubQueryService;

    @PostMapping
    public Response<ClubResponseDTO.CreateClubDTO> createClub(@RequestBody @Valid ClubRequestDTO.CreateClubDTO request) {
        Club club = clubCommandService.createClub(request);
        return Response.ok(ClubConverter.toCreateClubDTO(club));
    }

    @Operation(summary = "부스 상세 조회")
    @GetMapping("/{clubId}")
    public Response<ClubResponseDTO.GetClubDTO> getClub(@PathVariable Long clubId) {

        Club club = clubQueryService.getClub(clubId);
        return Response.ok(ClubConverter.toGetClubDTO(club));
    }
//
//    @Operation(summary = "부스배치도 조회", description = "장소별로 부스를 조회합니다.")
//    @GetMapping("/{place}")
//    public Response<ClubResponseDTO.GetAllClubDTO> getAllClub(@PathVariable String place) {
//        Club club = clubQueryService.getAllClub(place);
//        return Response.ok(ClubConverter.toGetAllClubDTO(club));
//    }
}