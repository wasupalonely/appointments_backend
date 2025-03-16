package com.juandmv.backend.auth;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class TokenJwtConfig {
    public static final String CONTENT_TYPE = "application/json";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    private static final String SECRET_STRING = "2e0fb93ab4f87288796d496055e1082bacd6ec27a462e4f46d2bafc7fd40c25daa1312588e68ce872f802546ff433ca6f506b1764b74e9f2ff8deb7486b544b6520e353d7ba53aec3fd9cebca7d06784e01e8d6bc9d168e5316a5e4091bc822210d3637eba98b1f9157af9020936974c37c262fd5145e7a04f66324ecc4493ad6cf84c0227ce10597c651062c2d7c7aff872b1bb82a96add0b0053f0d303c58b50a059c515416ebdeedfbd6c951728c42e7b07c68d740face02d16793513a29f6aaf46f3f48efd0d875f34ef1724c537d08def345d70ccd6ae31d032fe4fc345e76934d43ec5acffd68fb9623899cc34b9d790b1e5170dbd6e8916b3ed4eed83";
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
}