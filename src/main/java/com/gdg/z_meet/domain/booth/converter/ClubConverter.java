package com.gdg.z_meet.domain.booth.converter;

import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.dto.ClubResponseDTO;
import com.gdg.z_meet.domain.booth.entity.Category;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.entity.Place;

import java.util.List;
import java.util.stream.Collectors;

public class ClubConverter {

    public static Club toClub(ClubRequestDTO.CreateClubDTO request){

        return Club.builder()
                .place(Place.valueOf(request.getPlace()))
                .name(request.getName())
                .rep(request.getRep())
                .category(Category.valueOf(request.getCategory()))
                .account(request.getAccount())
                .time(request.getTime())
                .info(request.getInfo())
                .build();
    }

    public static ClubResponseDTO.CreateClubDTO toCreateClubDTO(Club club){

        return ClubResponseDTO.CreateClubDTO.builder()
                .clubId(club.getId())
                .build();
    }

    public static ClubResponseDTO.GetClubDTO toGetClubDTO(Club club){

        return ClubResponseDTO.GetClubDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .rep(club.getRep())
                .category(club.getCategory().toString())
                .account(club.getAccount())
                .time(club.getTime())
                .info(club.getInfo())
                .build();
    }

//    public static ClubResponseDTO.GetAllClubDTO toGetAllClubDTO(List<Club> clubList){
//
//        List<ClubResponseDTO.GetClubDTO> clubDTOS = clubList.stream()
//                .map(club -> ClubResponseDTO.GetClubDTO.builder()
//                        .clubId(club.getId())
//                        .name(club.getName())
//                        .rep(club.getRep())
//                        .category(club.getCategory().toString())
//                        .account(club.getAccount())
//                        .time(club.getTime())
//                        .info(club.getInfo())
//                        .build())
//                .collect(Collectors.toList());
//
//        return ClubResponseDTO.GetAllClubDTO.builder()
//                .clubList(clubDTOS)
//                .build();
//    }
}
