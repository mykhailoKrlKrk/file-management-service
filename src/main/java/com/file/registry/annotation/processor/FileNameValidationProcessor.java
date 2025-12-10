package com.file.registry.annotation.processor;

import com.file.registry.annotation.ValidFileName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

public class FileNameValidationProcessor
        implements ConstraintValidator<ValidFileName, MultipartFile> {

    private static final String FILE_NAME_PATTERN =
            "^[a-zA-Z0-9]+_[a-zA-Z0-9]+_\\d{4}-\\d{2}-\\d{2}\\.xml$";

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty() || Objects.isNull(file.getOriginalFilename())) {
            return false;
        }
        return file.getOriginalFilename().matches(FILE_NAME_PATTERN);
    }
}

