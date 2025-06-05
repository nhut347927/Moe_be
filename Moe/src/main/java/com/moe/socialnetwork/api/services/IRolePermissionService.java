package com.moe.socialnetwork.api.services;

import java.util.List;

import com.moe.socialnetwork.api.dtos.DeleteDTO;
import com.moe.socialnetwork.api.dtos.ListRolePerDTO;
import com.moe.socialnetwork.api.dtos.RolePermissionDTO;

public interface IRolePermissionService {
    List<RolePermissionDTO> getPermissionsByUser(String userCode);

    void createOrUpdatePermission(ListRolePerDTO dto);

    void deletePermission(DeleteDTO code);
}
