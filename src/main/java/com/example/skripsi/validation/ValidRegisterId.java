package com.example.skripsi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RegisterIdValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRegisterId {
    String message() default "Invalid ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
