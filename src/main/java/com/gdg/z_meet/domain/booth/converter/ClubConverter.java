package com.gdg.z_meet.domain.booth.converter;

import com.gdg.z_meet.domain.booth.dto.ClubRequestDTO;
import com.gdg.z_meet.domain.booth.dto.ClubResponseDTO;
import com.gdg.z_meet.domain.booth.entity.Category;
import com.gdg.z_meet.domain.booth.entity.Club;
import com.gdg.z_meet.domain.booth.entity.Place;

public class ClubConverter {

    public static Club toClub(ClubRequestDTO.ClubCreateDTO request){

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

    public static ClubResponseDTO.ClubCreateDTO toClubCreateDTO(Club club){

        return ClubResponseDTO.ClubCreateDTO.builder()
                .clubId(club.getId())
                .build();
    }
}
