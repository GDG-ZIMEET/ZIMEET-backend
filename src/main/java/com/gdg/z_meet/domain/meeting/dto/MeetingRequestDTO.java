package com.gdg.z_meet.domain.meeting.dto;

import com.gdg.z_meet.global.validation.annotation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MeetingRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class hiDto {
        Long toId;
        Long fromId;
    }

}
