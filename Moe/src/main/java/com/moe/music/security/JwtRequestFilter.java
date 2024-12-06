package com.moe.music.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moe.music.exception.AppException;
import com.moe.music.response.ResponseAPI;
import com.moe.music.service.CustomUserDetailsService;
import com.moe.music.service.TokenService;
import com.moe.music.utility.JwtUtil;

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

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		final String authorizationHeader = request.getHeader("Authorization");

		String email = null;
		String jwt = null;

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			try {
				email = tokenService.getEmailFromJwtToken(jwt);
			} catch (ExpiredJwtException e) {
				sendErrorResponse(response, "JWT token has expired. Please login again.",
						HttpServletResponse.SC_UNAUTHORIZED);
				return;
			} catch (AppException e) {
				sendErrorResponse(response, "Invalid or missing token. Please provide a valid token.",
						HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			try {
				if (tokenService.validateJwtToken(jwt)) {
					List<SimpleGrantedAuthority> authorities = jwtUtil.extractPermissions(jwt).stream()
							.map(SimpleGrantedAuthority::new).collect(Collectors.toList());

					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, authorities);
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				} else {
					sendErrorResponse(response, "Token is invalid", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			} catch (AppException e) {
				sendErrorResponse(response, "Token is invalid", HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
		ResponseAPI<String> res = new ResponseAPI<>();
		res.setCode(statusCode);
		res.setMessage(message);
		res.setData(null);

		response.setStatus(statusCode);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = mapper.writeValueAsString(res);
		response.getWriter().write(jsonResponse);
		response.getWriter().flush();
	}
}
