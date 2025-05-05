package com.easygofly.api.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api/login")
public class RefreshTokenController {
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private UserDetailsService userDetailsService;

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
		String refreshToken = request.get("refresh");
		System.out.println("refreshToken: " + refreshToken);
		try {
			Claims claims = jwtUtil.validateRefreshToken(refreshToken);
			String username = claims.getSubject();

			@SuppressWarnings("unused")
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			String newAccessToken = jwtUtil.generateAccessToken(username);

			return ResponseEntity.ok(Map.of("accessToken", "Bearer " + newAccessToken, "refreshToken", refreshToken));
			
		} catch (ExpiredJwtException e) {
		    System.out.println("Expired at: " + e.getClaims().getExpiration());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
		} catch (JwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
		}
	}
}
