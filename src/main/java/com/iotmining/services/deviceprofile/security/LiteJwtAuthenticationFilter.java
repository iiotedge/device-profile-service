package com.iotmining.services.deviceprofile.security;

import com.iotmining.common.base.context.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LiteJwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    @Value("${internal.api.key:default-internal-key}")
    private String internalApiKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String internalHeader = request.getHeader("X-Internal-API-Key");
            String authHeader = request.getHeader("Authorization");

            if (internalApiKey.equals(internalHeader)) {
                UsernamePasswordAuthenticationToken systemAuth = new UsernamePasswordAuthenticationToken(
                        "SYSTEM_SERVICE", null, List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")));
                SecurityContextHolder.getContext().setAuthentication(systemAuth);

                String systemTenantId = request.getHeader("X-Tenant-ID");
                if (systemTenantId != null && !systemTenantId.isBlank()) {
                    TenantContext.setTenantId(systemTenantId);
                }
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Claims claims = Jwts.parser().verifyWith(getSigningKey()).build()
                        .parseSignedClaims(token).getPayload();

                List<String> roles = claims.get("role", List.class);
                List<SimpleGrantedAuthority> authorities = roles == null
                        ? List.of()
                        : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(claims, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                Object tIdObj = claims.get("tenantId");
                if (tIdObj == null) {
                    tIdObj = claims.get("tenant_id");
                }
                String tenantId = tIdObj != null ? tIdObj.toString() : request.getHeader("X-Tenant-ID");

                if (tenantId != null && !tenantId.isBlank()) {
                    TenantContext.setTenantId(tenantId);
                } else {
                    boolean isSuperAdmin = authorities.stream().anyMatch(a -> a.getAuthority().contains("SUPER_ADMIN"));
                    if (isSuperAdmin) {
                        log.debug("Super Admin authenticated. No Tenant ID required.");
                    } else {
                        log.warn("User authenticated but NO Tenant ID found in JWT or Headers. Claims: {}", claims.keySet());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Authentication context extraction failed: {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
