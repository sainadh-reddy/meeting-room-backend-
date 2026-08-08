package com.meetingroom.monolith.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(jwtToken)) {
                userEmail = jwtService.extractUsername(jwtToken);
                Long userId = jwtService.extractUserId(jwtToken);
                String role = jwtService.extractRole(jwtToken);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + (role != null ? role.toUpperCase() : "EMPLOYEE"))
                    );

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, null, authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Wrap request with custom header wrapper to pass headers expected by migrated controllers
                    HeaderMapRequestWrapper wrappedRequest = new HeaderMapRequestWrapper(request);
                    if (userId != null) {
                        wrappedRequest.addHeader("X-Auth-UserId", String.valueOf(userId));
                    }
                    if (userEmail != null) {
                        wrappedRequest.addHeader("X-Auth-User", userEmail);
                    }
                    if (role != null) {
                        wrappedRequest.addHeader("X-Auth-Role", role);
                    }

                    filterChain.doFilter(wrappedRequest, response);
                    return;
                }
            }
        } catch (Exception e) {
            // Invalid token, proceed down filter chain unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}
