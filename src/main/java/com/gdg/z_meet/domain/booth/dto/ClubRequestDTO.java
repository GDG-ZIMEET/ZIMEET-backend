package com.gdg.z_meet.domain.booth.dto;

import com.gdg.z_meet.domain.booth.entity.Category;
import com.gdg.z_meet.domain.booth.entity.Place;
import com.gdg.z_meet.global.validation.annotation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class ClubRequestDTO {

    @Getter
    public static class ClubCreateDTO {

        @NotNull
        @ValidEnum(enumClass = Place.class)
        String place;

        @NotBlank
        String name;

        @NotBlank
        String rep;

        @NotNull
        @ValidEnum(enumClass = Category.class)
        String category;

        String account;

        @NotBlank
        String time;

        @NotBlank
        String info;
    }
}
