package com.moe.music.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserJPA userJPA;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user;

		try {
			user = userJPA.findByEmail(email);
		} catch (Exception e) {
			throw new AppException("Error retrieving user by email: " + email, 500);
		}

		if (user == null) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}

		if (!user.getIsActive()) {
			throw new AppException("User account is not active", 403);
		}

		try {
			if (user.getRole() != null) {
				Hibernate.initialize(user.getRole().getRolePermission());
			}
		} catch (Exception e) {
			throw new AppException("Error initializing user roles for email: " + email, 500);
		}

		List<GrantedAuthority> authorities;
		try {

			if (user.getRole() != null && user.getRole().getRolePermission() != null) {
				authorities = user.getRole().getRolePermission().stream().map(
						rolePermission -> new SimpleGrantedAuthority(rolePermission.getPermission().getActionName()))
						.collect(Collectors.toList());
			} else {
				authorities = List.of();
			}
		} catch (Exception e) {
			throw new AppException("Error processing user roles for email: " + email, 500);
		}

		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(),
				user.getIsActive(), true, true, true, authorities);
	}
}
