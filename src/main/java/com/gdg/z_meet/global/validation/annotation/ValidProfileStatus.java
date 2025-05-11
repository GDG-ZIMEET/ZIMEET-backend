package com.gdg.z_meet.global.validation.annotation;

import com.gdg.z_meet.global.validation.validator.ProfileStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = ProfileStatusValidator.class)
public @interface ValidProfileStatus {
    String message() default "status는 ACTIVE 또는 INACTIVE만 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
