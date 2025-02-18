package com.gdg.z_meet.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MeetingRequestDTO {


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class hiDto {
        Long toId;
        Long fromId;
    }


    @Getter
    public static class CreateTeamDTO {

        @NotBlank
        @Size(min = 1, max = 8)
        String name;

        @NotNull
        List<Long> teamMember;
    }

}
