package com.example.skripsi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegisterIdValidator implements ConstraintValidator<ValidRegisterId, String> {

    @Override
    public boolean isValid(String registerId, ConstraintValidatorContext context) {
        if (registerId == null || registerId.trim().isEmpty()) {
            return true;
        }

        int length = registerId.length();

        if (length == 5) {
            return registerId.startsWith("D");
        } else if (length == 10) {
            return registerId.startsWith("2");
        } else {
            return false;
        }
    }
}