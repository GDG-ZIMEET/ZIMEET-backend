package com.gdg.z_meet.global.validation.validator;

import com.gdg.z_meet.domain.user.entity.enums.ProfileStatus;
import com.gdg.z_meet.global.validation.annotation.ValidProfileStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProfileStatusValidator implements ConstraintValidator<ValidProfileStatus, ProfileStatus> {
    private ValidProfileStatus annotation;

    @Override
    public void initialize(ValidProfileStatus constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(ProfileStatus value, ConstraintValidatorContext context) {
        return value == ProfileStatus.ACTIVE || value == ProfileStatus.INACTIVE;
    }
}
