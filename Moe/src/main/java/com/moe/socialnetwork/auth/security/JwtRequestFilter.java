package com.moe.socialnetwork.auth.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moe.socialnetwork.auth.services.impl.TokenServiceImpl;
import com.moe.socialnetwork.common.response.ResponseAPI;
import com.moe.socialnetwork.exception.AppException;

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
    private TokenServiceImpl tokenService;

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
