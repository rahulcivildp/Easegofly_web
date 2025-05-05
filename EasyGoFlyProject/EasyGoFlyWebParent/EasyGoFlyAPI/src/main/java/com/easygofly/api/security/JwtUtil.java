package com.easygofly.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
	private static final String SECRET_KEY = "baffe33fd15ecac2661b2c6f37074e681f378b42c2661b2c6f37074e681f378b42d15ecac2661b2c6f370";
	private static final String REFRESH_KEY = "0639f30edabf1f1cc6e18581d766e65ea9275cc88b8852386e2c26314065414154d499f7e5a82a288a8229cd656b34ede9fcd84abfb3eddd61288597a22e9d0a";
	private static final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000; // 30 days
	private static final String TOKEN_PREFIX = "Bearer ";
	private static final String HEADER_STRING = "Authorization";

	@SuppressWarnings("deprecation")
	public static String generateToken(Authentication auth) {
		return Jwts.builder().setSubject(((EasegoflyPhoneCustomerDetails) auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + 2 * 60 * 1000))
				.signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes()).compact();
	}

	@SuppressWarnings("deprecation")
	public String generateAccessToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes()).compact();
	}

	@SuppressWarnings("deprecation")
	public static String generateRefreshToken(Authentication auth) {
		Date expiry = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

		return Jwts.builder().setSubject(((EasegoflyPhoneCustomerDetails) auth.getPrincipal()).getUsername())
				.setIssuedAt(new Date()).setExpiration(expiry)
				.signWith(SignatureAlgorithm.HS256, REFRESH_KEY.getBytes()).compact();
	}

	public static String validateToken(HttpServletRequest request) {
		String token = request.getHeader(HEADER_STRING);
		if (token != null) {
		    try {
	            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	            Claims claims = Jwts.parserBuilder()
	                    .setSigningKey(key)
	                    .setAllowedClockSkewSeconds(60) // 👈 allow up to 60 seconds skew
	                    .build()
	                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
	                    .getBody();

	            return claims.getSubject();
	        } catch (Exception e) {
	            System.out.println("JWT validation error: " + e.getMessage());
	            return null;
	        }
		    
//			return Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
//					.getBody().getSubject();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static String validateRefreshToken(HttpServletRequest request) {
		String token = request.getHeader(HEADER_STRING);
		if (token != null) {
			return Jwts.parser().setSigningKey(REFRESH_KEY.getBytes()).parseClaimsJws(token).getBody().getSubject();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public Claims validateRefreshToken(String token) throws JwtException {
		return Jwts.parser().setSigningKey(REFRESH_KEY.getBytes()).parseClaimsJws(token).getBody();
	}
}
