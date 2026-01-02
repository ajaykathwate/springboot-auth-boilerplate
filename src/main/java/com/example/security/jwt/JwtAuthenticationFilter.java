package com.example.security.jwt;

import com.example.common.dto.ApiErrorResponseCreator;
import com.example.security.exception.AbsentBearerHeaderException;
import com.example.security.exception.JwtTokenHasNoUserEmailException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final ApiErrorResponseCreator errorResponseCreator;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest httpRequest,
                                    @NonNull final HttpServletResponse httpResponse,
                                    @NonNull final FilterChain filterChain) throws IOException {

        try {
            var authenticationToken = jwtAuthenticationProvider.get(httpRequest);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(httpRequest, httpResponse);

        } catch (AbsentBearerHeaderException ex) {
            handleAuthenticationException(httpResponse, "Authentication failed: invalid authorization header", ex, HttpServletResponse.SC_UNAUTHORIZED);
        } catch (ExpiredJwtException ex) {
            handleAuthenticationException(httpResponse, "Authentication failed: token expired", ex, HttpServletResponse.SC_UNAUTHORIZED);
        } catch (JwtTokenHasNoUserEmailException ex) {
            handleAuthenticationException(httpResponse, "Authentication failed: invalid token format", ex, HttpServletResponse.SC_UNAUTHORIZED);
        } catch (UsernameNotFoundException ex) {
            handleAuthenticationException(httpResponse, "Authentication failed: user not found", ex, HttpServletResponse.SC_UNAUTHORIZED);
        } catch (ServletException | RuntimeException ex) {
            handleAuthenticationException(httpResponse, "Authentication failed: internal error", ex, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleAuthenticationException(HttpServletResponse httpResponse,
                                               String errorMessage,
                                               Exception exception,
                                               int statusCode) throws IOException {

        SecurityContextHolder.clearContext();

        var body = errorResponseCreator.buildResponse(
            errorMessage,
            false,
            HttpStatus.valueOf(statusCode)
        );

        httpResponse.setStatus(statusCode);
        httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpResponse.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(httpResponse.getWriter(), body);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }
}
