package com.moe.music.dto;

import lombok.Data;

@Data
public class FieldErrorDTO {
    private String field;
    private String errorMessage;
    private String errorCode;

    public FieldErrorDTO(String field, String errorMessage, String errorCode) {
        this.field = field;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

}
