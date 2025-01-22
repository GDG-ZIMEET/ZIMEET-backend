package com.gdg.z_meet.domain.booth.dto;

import com.gdg.z_meet.domain.booth.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ClubResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateClubDTO {
        Long clubId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetClubDTO {
        Long clubId;
        String name;
        String rep;
        String category;
        String account;
        String time;
        String info;
    }

//    @Builder
//    @Getter
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class GetAllClubDTO {
//        List<GetClubDTO> clubList;
//    }
}
