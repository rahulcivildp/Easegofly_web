package com.easygofly.api.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		   String user = JwtUtil.validateToken(request);

	        if (user != null) {
	            SecurityContextHolder.getContext().setAuthentication(
	                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
	            );
	        }

	        chain.doFilter(request, response);
	}

	
}
