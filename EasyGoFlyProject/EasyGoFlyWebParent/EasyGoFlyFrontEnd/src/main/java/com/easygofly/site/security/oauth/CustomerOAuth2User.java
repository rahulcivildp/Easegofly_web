package com.easygofly.site.security.oauth;

import java.util.Collection;
import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@AutoConfiguration
public class CustomerOAuth2User implements OAuth2User {

	private String clientName;
	private OAuth2User oauth2User;
	
	
	public CustomerOAuth2User(OAuth2User oauth2User, String clientName) {
		this.oauth2User = oauth2User;
		this.clientName = clientName;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return oauth2User.getAttributes();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return oauth2User.getAuthorities();
	}

	@Override
	public String getName() {
		return oauth2User.getAttribute("name"); 
	}
	
	public String getEmail() {
		System.out.println("Email: " + oauth2User.getAttribute("email"));
		return oauth2User.getAttribute("email");
	}

	public String getFullName() {
		return oauth2User.getAttribute("name");
	}

	public String getClientName() {
		return clientName;
	}

}
