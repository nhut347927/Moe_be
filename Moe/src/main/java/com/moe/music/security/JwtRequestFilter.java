package com.moe.music.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.moe.music.service.CustomUserDetailsService;
import com.moe.music.service.TokenService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		final String authorizationHeader = request.getHeader("Authorization");
		String email = null;
		String token = null;
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			token = authorizationHeader.substring(7);
			try {
				email = tokenService.getEmailFromJwtToken(token);
			} catch (ExpiredJwtException e) {
				System.out.println("Token has expired");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
				return;
			} catch (Exception e) {
				System.out.println("Invalid token: " + e.getMessage());
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
				return;
			}
		}

		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			authenticateUser(email, token);
		}

		chain.doFilter(request, response);
	}

	private void authenticateUser(String email, String token) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		if (tokenService.validateJwtToken(token)) {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
					null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
	}

}
