package com.easygofly.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

public class JwtUtil {
	private static final String SECRET_KEY = "baffe33fd15ecac2661b2c6f37074e681f378b42c2661b2c6f37074e681f378b42d15ecac2661b2c6f370";
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";

    @SuppressWarnings("deprecation")
	public static String generateToken(Authentication auth) {
        return Jwts.builder()
                .setSubject(((EasegoflyPhoneCustomerDetails) auth.getPrincipal()).getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes())
                .compact();
    }

    @SuppressWarnings("deprecation")
	public static String validateToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody()
                    .getSubject();
        }
        return null;
    }
}
