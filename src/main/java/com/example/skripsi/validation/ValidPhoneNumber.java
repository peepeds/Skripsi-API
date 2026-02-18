package com.example.skripsi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Phone number must be started with '08' and minimum contains 8 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
