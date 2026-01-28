package com.ssafy.virtudy.global.event.resolver;

import com.ssafy.virtudy.global.event.annotation.EnumPattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class EnumPatternValidator implements ConstraintValidator<EnumPattern, Object> {

    private Class<? extends Enum<?>> enumClass;
    private boolean ignoreCase;

    @Override
    public void initialize(EnumPattern constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            String valueStr = (String) value;
            return Arrays.stream(enumClass.getEnumConstants())
                    .anyMatch(e -> ignoreCase
                            ? e.name().equalsIgnoreCase(valueStr)
                            : e.name().equals(valueStr));
        }

        return false;
    }
}
