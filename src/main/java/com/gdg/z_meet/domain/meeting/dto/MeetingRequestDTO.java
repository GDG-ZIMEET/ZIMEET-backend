package com.gdg.z_meet.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

public class MeetingRequestDTO {

    @Getter
    public static class CreateTeamDTO {

        @NotBlank
        @Size(min = 1, max = 8)
        String name;

        @NotNull
        List<Long> teamMember;
    }
}
