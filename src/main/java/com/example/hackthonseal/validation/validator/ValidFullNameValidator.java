package com.example.hackthonseal.validation.validator;

import com.example.hackthonseal.validation.annotation.ValidFullName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidFullNameValidator implements ConstraintValidator<ValidFullName, String> {

    @Override
    public void initialize(ValidFullName annotation) {
        ConstraintValidator.super.initialize(annotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }
}

