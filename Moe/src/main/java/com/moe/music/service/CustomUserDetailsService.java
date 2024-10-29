package com.moe.music.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserJPA userJPA;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userJPA.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(),
				user.isActive(), true, // accountNonExpired
				true, // credentialsNonExpired
				true, // accountNonLocked
				// roles
				new ArrayList<>() // Add roles if you have any
		);
	}
}
