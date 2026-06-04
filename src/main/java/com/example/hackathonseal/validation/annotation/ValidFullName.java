package com.example.hackathonseal.validation.annotation;

import com.example.hackathonseal.validation.validator.ValidFullNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFullNameValidator.class)
@Documented
public @interface ValidFullName {
    String message() default "Full name must be between 2 and 100 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

