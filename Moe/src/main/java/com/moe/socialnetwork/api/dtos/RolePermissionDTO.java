package com.moe.socialnetwork.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDTO {
    private String code;
    @NotBlank(message = "User ID cannot be blank")
    private String userCode;
    @NotBlank(message = "Role ID cannot be blank")
    private String roleCode;
    private Boolean canView = false;
    private Boolean canInsert = false;
    private Boolean canUpdate = false;
    private Boolean canDelete = false;
    private Boolean canRestore = false;

}