package com.moe.socialnetwork.api.dtos;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDTO {
    @NotBlank(message = "Code cannot be blank")
    private String code;
}
