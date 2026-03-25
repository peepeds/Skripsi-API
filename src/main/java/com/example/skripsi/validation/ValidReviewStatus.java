package com.example.skripsi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReviewStatusValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReviewStatus {
    String message() default "Review status must be APPROVED or REJECTED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

