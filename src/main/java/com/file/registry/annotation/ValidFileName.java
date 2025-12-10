package com.file.registry.annotation;

import com.file.registry.annotation.processor.FileNameValidationProcessor;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileNameValidationProcessor.class)
public @interface ValidFileName {

    String message() default "Invalid file name format. Expected: customer_type_yyyy-MM-dd.xml";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
