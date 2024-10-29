package com.moe.music.utility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.moe.music.dto.FieldErrorDTO;

public class ValidationUtil {

    /**
     * Chuyển đổi BindingResult thành danh sách FieldErrorDTO chứa thông tin chi tiết
     * về lỗi trong quá trình xác thực.
     * 
     * @param errors - BindingResult chứa các lỗi xác thực
     * @return List<FieldErrorDTO> - Danh sách lỗi chi tiết
     */
    public static List<FieldErrorDTO> validateErrors(BindingResult errors) {
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();

        if (errors.hasErrors()) {
            for (ObjectError error : errors.getAllErrors()) {
                // Xử lý lỗi trên các trường cụ thể
                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error;
                    String field = fieldError.getField();
                    String errorMessage = fieldError.getDefaultMessage();
                    String errorCode = fieldError.getCode(); // Mã lỗi cụ thể

                    fieldErrors.add(new FieldErrorDTO(field, errorMessage, errorCode));
                } else {
                    // Xử lý các lỗi áp dụng cho toàn bộ object
                    String errorMessage = error.getDefaultMessage();
                    String errorCode = error.getCode(); // Mã lỗi cụ thể cho toàn bộ object
                    fieldErrors.add(new FieldErrorDTO("global", errorMessage, errorCode));
                }
            }
        }

        return fieldErrors;
    }
}
