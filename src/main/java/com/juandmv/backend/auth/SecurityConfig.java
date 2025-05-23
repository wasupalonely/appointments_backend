package com.juandmv.backend.auth;

import com.juandmv.backend.auth.filter.JwtAuthenticationFilter;
import com.juandmv.backend.auth.filter.JwtValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(
                        authz ->
                                //TODO: Agregar las rutas con sus roles y validaciones
                                authz
                                        // AUTH
                                        .requestMatchers("/auth/**").permitAll()

                                        // DOCS
                                        .requestMatchers("/v3/api-docs/**").permitAll()
                                        .requestMatchers("/swagger-ui/**").permitAll()
					                    .requestMatchers("/api-docs").permitAll()

                                        // APPOINTMENTS
                                        .requestMatchers("/appointments/**").authenticated()

                                        // USERS
                                        .requestMatchers("/users/**").authenticated()

                                        // SPECIALTIES
                                        .requestMatchers(HttpMethod.GET, "/specialties/**").authenticated()
                                        .requestMatchers(HttpMethod.POST, "/specialties/**").hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/specialties/**").hasRole("ADMIN")

                                        // APPOINTMENT TYPES
                                        .requestMatchers(HttpMethod.GET, "/appointment-types/**").authenticated()
                                        .requestMatchers(HttpMethod.POST, "/appointment-types/**").hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/appointment-types/**").hasRole("ADMIN")

                                        // AVAILABILITIES
                                        .requestMatchers(HttpMethod.GET, "/availabilities/**").authenticated()
                                        .requestMatchers(HttpMethod.POST, "/availabilities/**").hasAnyRole("ADMIN", "DOCTOR")
                                        .requestMatchers(HttpMethod.PUT, "/availabilities/**").hasAnyRole("ADMIN", "DOCTOR")
                                        .requestMatchers(HttpMethod.DELETE, "/availabilities/**").hasAnyRole("ADMIN", "DOCTOR")

                                        // UNAVAILABILITIES
                                        .requestMatchers(HttpMethod.GET, "/unavailabilities/**").authenticated()
                                        .requestMatchers(HttpMethod.POST, "/unavailabilities/**").hasAnyRole("ADMIN", "DOCTOR")
                                        .requestMatchers(HttpMethod.PUT, "/unavailabilities/**").hasAnyRole("ADMIN", "DOCTOR")
                                        .requestMatchers(HttpMethod.DELETE, "/unavailabilities/**").hasAnyRole("ADMIN", "DOCTOR")

                                        // ONLY ADMIN
                                        .requestMatchers("/appointment-types/**").hasRole("ADMIN")
                                        .requestMatchers("/physical-locations/**").hasRole("ADMIN")
                                        .anyRequest().authenticated())
                  .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtValidationFilter(authenticationManager()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://192.168.1.15:3000")); // Específica tu origen
        config.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization")); // Importante para JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
