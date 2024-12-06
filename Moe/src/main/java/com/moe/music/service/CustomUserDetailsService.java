package com.moe.music.service;

import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
		Optional<User> user;

		try {
			user = userJPA.findByEmail(email);
		} catch (Exception e) {
			throw new AppException("Error retrieving user by email: " + email,
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}

		if (Boolean.TRUE.equals(user.get().getIsDeleted())) {
			throw new AppException("User account has been deleted!", HttpStatus.FORBIDDEN.value());
		}

		try {
			User foundUser = user.get();
			if (foundUser.getRole() != null && foundUser.getRole().getRolePermission() != null) {
				Hibernate.initialize(foundUser.getRole().getRolePermission());
			} else {
				throw new AppException(
						"Warning: User has no assigned role or permissions. Assigning default permissions if needed.",
						HttpStatus.BAD_REQUEST.value());
			}
		} catch (Exception e) {
			throw new AppException("Error initializing roles and permissions for email: " + email,
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return user.get();
	}

}
