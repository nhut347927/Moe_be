package com.moe.music.jwt;

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
import com.moe.music.utility.AuthorityUtil;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserJPA userJPA;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		if (email == null || email.isBlank()) {
			return null;
		}

		Optional<User> optionalUser;
		try {
			optionalUser = userJPA.findByEmail(email);
		} catch (Exception e) {
			throw new AppException("Error retrieving user by email: " + email,
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		if (optionalUser.isEmpty()) {
			return null;
		}

		User user = optionalUser.get();

		if (Boolean.TRUE.equals(user.getIsDeleted())) {
			throw new AppException("User account has been deleted!", HttpStatus.FORBIDDEN.value());
		}

		if (!(user.getRolePermissions() == null || user.getRolePermissions().isEmpty())) {
			Hibernate.initialize(AuthorityUtil.convertToAuthorities(user.getRolePermissions()));
		}

		return user;
	}

}