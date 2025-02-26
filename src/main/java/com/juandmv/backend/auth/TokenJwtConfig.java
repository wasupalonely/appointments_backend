package com.juandmv.backend.auth;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

public class TokenJwtConfig {
    public static final String CONTENT_TYPE = "application/json";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
}