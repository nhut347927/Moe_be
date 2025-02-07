package com.moe.music.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moe.music.exception.AppException;
import com.moe.music.model.User;
import com.moe.music.response.ResponseAPI;
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

    @Value("${app.expiration}")
    private Long jwtExpirationMs;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        String email = null;
        String jwt = tokenService.extractAccessTokenFromCookie(request);

        try {
            if (jwt != null && tokenService.validateJwtToken(jwt)) {
                email = tokenService.getEmailFromJwtToken(jwt);
            } else {
                String refreshToken = tokenService.extractRefreshTokenFromCookie(request);
                if (refreshToken != null && tokenService.validateRefreshToken(refreshToken)) {
                    User user = tokenService.getUserFromRefreshToken(refreshToken);
                    if (user != null) {
                        email = user.getEmail();

                        String accessToken = tokenService.generateJwtToken(user);
                        int maxAgeAccessToken = (int) (jwtExpirationMs / 1000);

                        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .maxAge(maxAgeAccessToken)
                                .sameSite("Lax") // Đảm bảo thuộc tính SameSite để tương thích với trình duyệt
                                .build();
                        
                        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, "Your refresh token has expired. Please log in again.", 401);
            return;
        } catch (AppException e) {
            sendErrorResponse(response, "An application error occurred: " + e.getMessage(), 500);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
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
