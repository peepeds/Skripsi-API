package com.example.skripsi.validation;

import com.example.skripsi.entities.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReviewStatusValidator implements ConstraintValidator<ValidReviewStatus, CompanyRequestStatus> {

    @Override
    public boolean isValid(CompanyRequestStatus value, ConstraintValidatorContext context) {
        if (value == null) return true; // null is handled by @NotNull separately
        return value == CompanyRequestStatus.APPROVED || value == CompanyRequestStatus.REJECTED;
    }
}

