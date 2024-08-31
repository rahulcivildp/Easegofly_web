package com.easygofly.api.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/login");
    }
    
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
            UserCredentials creds = new ObjectMapper().readValue(request.getInputStream(), UserCredentials.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            Collections.emptyList()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	@Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        String token = JwtUtil.generateToken(authResult);
        response.addHeader("Authorization", "Bearer " + token);
    }

    private static class UserCredentials {
        private String username;
        private String password;
        
		public String getUsername() {
			return username;
		}
		@SuppressWarnings("unused")
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		@SuppressWarnings("unused")
		public void setPassword(String password) {
			this.password = password;
		}

        
    }

}
