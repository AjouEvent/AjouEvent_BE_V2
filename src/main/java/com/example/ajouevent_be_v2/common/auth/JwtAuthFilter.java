package com.example.ajouevent_be_v2.common.auth;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import com.example.ajouevent_be_v2.common.exception.auth.AuthException;
import com.example.ajouevent_be_v2.common.util.JwtUtil;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                jwtUtil.validateToken(token);
                Long memberId = jwtUtil.getMemberIdFromToken(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(String.valueOf(memberId));
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (AuthException ex) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, ex.getErrorCode());
            return;
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, AuthErrorCode.UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode)
        throws IOException {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }
}
