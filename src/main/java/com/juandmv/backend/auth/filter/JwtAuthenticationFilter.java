package com.juandmv.backend.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juandmv.backend.auth.TokenJwtConfig;
import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.exceptions.BadRequestException;
import com.juandmv.backend.models.dto.AuthRequestDto;
import com.juandmv.backend.models.entities.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String documentType = null;
        String documentNumber = null;
        String password = null;

        try {
            AuthRequestDto authRequest = new ObjectMapper().readValue(request.getInputStream(), AuthRequestDto.class);

            documentType = authRequest.getDocumentType();
            documentNumber = authRequest.getDocumentNumber();
            password = authRequest.getPassword();

            if (documentType == null || documentNumber == null || password == null) {
                throw new BadRequestException("Tipo de documento, número de documento y contraseña son obligatorios");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Usamos el formato: documentType + ":" + documentNumber como el username para autenticación
        String authUsername = documentType + ":" + documentNumber;
        String authPassword = password;

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authUsername, authPassword);
        return this.authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        CustomUserDetails user = (CustomUserDetails) authResult.getPrincipal();
        String username = user.getUsername(); // Ahora contiene documentType:documentNumber
        Long userId = user.getId();
        String email = user.getEmail();
        DocumentType documentType = user.getDocumentType();
        String documentNumber = user.getDocumentNumber();

        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        Claims claims = Jwts
                .claims()
                .add("roles", new ObjectMapper().writeValueAsString(roles))
                .add("userId", userId.toString())
                .add("email", email)
                .add("documentType", documentType)
                .add("documentNumber", documentNumber)
                .build();

        String jwt = Jwts.builder()
                .subject(username)
                .claims(claims)
                .signWith(TokenJwtConfig.SECRET_KEY)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000 * 3))
                .compact();

        response.addHeader(TokenJwtConfig.HEADER_STRING, TokenJwtConfig.TOKEN_PREFIX + jwt);

        Map<String, String> body = new HashMap<>();
        body.put("token", jwt);
        body.put("username", username);
        body.put("email", email);
        body.put("id", userId.toString());
        body.put("documentType", documentType.toString());
        body.put("documentNumber", documentNumber);
        body.put("role", new ObjectMapper().writeValueAsString(roles));

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Tipo de documento y/o número de documento incorrectos");
        body.put("error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(TokenJwtConfig.CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}