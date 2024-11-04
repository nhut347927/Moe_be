package com.moe.music.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.exception.AppException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserJPA userJPA;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userJPA.findByEmail(email);
		if (user == null) {
			throw new AppException("User not found with email: " + email, 203);
		}

		List<GrantedAuthority> authorities = user.getRole().getRolePermission().stream()
				.map(rolePermission -> new SimpleGrantedAuthority(rolePermission.getPermission().getActionName()))
				.collect(Collectors.toList());

		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(),
				user.getIsActive(), true, // accountNonExpired
				true, // credentialsNonExpired
				true, // accountNonLocked
				authorities // Thêm vai trò vào đây
		);
	}
}
