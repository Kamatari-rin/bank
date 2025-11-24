package com.example.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class LoginMetricsFilter extends OncePerRequestFilter {

    private final AuthMetrics authMetrics;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        filterChain.doFilter(request, response);

        String path = request.getRequestURI();
        if (path.startsWith("/actuator")) {
            return;
        }

        int status = response.getStatus();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = extractUsername(auth);

        // 2xx/3xx + аутентифицированный principal = "успешный логин"
        if (auth != null && auth.isAuthenticated() && status >= 200 && status < 400) {
            authMetrics.loginSuccess(username);
        }

        // 401/403 = неуспешный логин (даже если principal null/anonymous)
        if (status == 401 || status == 403) {
            authMetrics.loginFailure(username);
        }
    }

    private String extractUsername(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth && auth.isAuthenticated()) {
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                    .or(() -> Optional.ofNullable(jwt.getClaimAsString("email")))
                    .orElse(jwt.getSubject());
        }
        return "unknown";
    }
}
