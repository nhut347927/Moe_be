package com.moe.music.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.moe.music.model.RolePermission;

public class AuthorityUtil {

	/**
	 * Chuyển đổi danh sách RolePermission thành danh sách quyền dạng String.
	 *
	 * @param rolePermissions Danh sách RolePermission.
	 * @return Set các quyền dưới dạng String.
	 */
	public static Set<String> convertToAuthorities(List<RolePermission> rolePermissions) {

		return Optional.ofNullable(rolePermissions).orElse(Collections.emptyList()).stream().flatMap(rolePermission -> {

			if (rolePermission == null || rolePermission.getRole() == null)
				return Stream.empty();

			String roleName = rolePermission.getRole().getRoleName();
			if (roleName == null || roleName.isEmpty())
				return Stream.empty();

			List<String> actions = Arrays
					.asList(rolePermission.getCanView() ? String.format("%s_VIEW", roleName) : null,
							rolePermission.getCanInsert() ? String.format("%s_INSERT", roleName) : null,
							rolePermission.getCanUpdate() ? String.format("%s_UPDATE", roleName) : null,
							rolePermission.getCanDelete() ? String.format("%s_DELETE", roleName) : null,
							rolePermission.getCanRestore() ? String.format("%s_RESTORE", roleName) : null)
					.stream().filter(Objects::nonNull).collect(Collectors.toList());

			return actions.stream();
		}).collect(Collectors.toSet());
	}
}
