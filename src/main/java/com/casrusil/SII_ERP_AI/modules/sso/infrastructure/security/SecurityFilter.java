package com.casrusil.SII_ERP_AI.modules.sso.infrastructure.security;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de Seguridad para autenticación JWT.
 * 
 * <p>
 * Intercepta las peticiones HTTP para validar la presencia y corrección
 * del token JWT en el encabezado Authorization.
 * 
 * <p>
 * Si el token es válido:
 * <ul>
 * <li>Extrae la identidad del usuario y la empresa.</li>
 * <li>Configura el contexto de seguridad de Spring
 * (SecurityContextHolder).</li>
 * <li>Inicializa el contexto de empresa (CompanyContext) para
 * multi-tenancy.</li>
 * </ul>
 * 
 * @see JwtService
 * @see CompanyContext
 * @since 1.0
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getRequestURI().startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            CompanyId companyId = jwtTokenProvider.getCompanyIdFromToken(token);

            // Set Spring Security Context (for @PreAuthorize if needed later)
            // We use a simple token here, but in a real app we might load UserDetails
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    jwtTokenProvider.getUserIdFromToken(token), null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Set CompanyContext using ScopedValue
            ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                    .run(() -> {
                        try {
                            filterChain.doFilter(request, response);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
