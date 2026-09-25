package com.groupa.digitalbackendapplication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupa.digitalbackendapplication.domain.dto.helper.TokenClaims;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ErrorResponse;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final LoginSessionService loginSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        TokenClaims claims = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            claims = tokenService.extractTokenClaims(token);
        }

        try {
            if(claims != null && claims.getUsername() != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if(claims.getTenancyId() == null || claims.getUsername() == null){
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Invalid token");
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getUsername());
                String activeSessionId = loginSessionService.getActiveSessionId(UUID.fromString(claims.getTenancyId()));

                if(!activeSessionId.equals(claims.getUserSessionId())){
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Invalid token");
                    return;
                }
                if(tokenService.isTokenValid(token, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }
            filterChain.doFilter(request, response);

        } catch (BadRequestException e) {
            log.error("Use the right access token");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid token");
            return;
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Session expired. Please log in again.\",\"statusCode\":401}");
            return;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid token. Please log in again.\",\"statusCode\":401}");
            return;
        } catch (Exception e) {
            log.error("Exception occurred while processing token: {}", e.getMessage());
            formatResponse(response,"Something went wrong",HttpStatus.INTERNAL_SERVER_ERROR,HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void formatResponse(HttpServletResponse response, String message, HttpStatus httpStatus, int httpServletResponse) throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .statusCode(httpStatus.value())
                .message(message)
                .build();

        response.setStatus(httpServletResponse);
        response.setContentType("application/json");

        response.getWriter()
                .write(new ObjectMapper().writeValueAsString(error));
    }
}
