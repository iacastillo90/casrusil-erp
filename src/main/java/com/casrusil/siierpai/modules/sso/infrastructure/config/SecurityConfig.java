package com.casrusil.siierpai.modules.sso.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final com.casrusil.siierpai.modules.sso.infrastructure.security.SecurityFilter securityFilter;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(com.casrusil.siierpai.modules.sso.infrastructure.security.SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
        log.info("🔥 CARGANDO CONFIGURACIÓN DE SEGURIDAD PERSONALIZADA 🔥");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF: No es necesario para APIs REST stateless con JWT
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configurar CORS explícitamente usando el Bean definido abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Gestión de Sesiones: STATELESS (Crucial para evitar jsessionid y
                // redirecciones)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Reglas de Autorización
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso público a endpoints de autenticación y Swagger si lo usas
                        .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                        // Permitir OPTIONS (pre-flight requests de CORS)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())

                // 5. Agregar filtro JWT antes del filtro de autenticación de usuario/password
                .addFilterBefore(securityFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        // 6. IMPORTANTE: No activar formLogin() ni httpBasic() para evitar
        // redirecciones a /login

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (El puerto de tu Frontend Next.js)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers permitidos (Necesario para JWT y tenant)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-ID"));

        // Permitir credenciales (cookies, headers de auth)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
