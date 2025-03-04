package com.moe.music.customvalidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileSizeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileSize {
    String message() default "File size exceeds limit!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    long max(); // Giới hạn dung lượng
}
