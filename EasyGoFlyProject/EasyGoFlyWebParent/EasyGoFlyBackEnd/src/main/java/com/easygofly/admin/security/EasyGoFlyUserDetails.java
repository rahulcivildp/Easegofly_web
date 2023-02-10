package com.easygofly.admin.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.easygofly.entity.Role;
import com.easygofly.entity.User;



@SuppressWarnings("serial")
@Component
@AutoConfiguration
public class EasyGoFlyUserDetails implements UserDetails {

	private User user;
	
	public EasyGoFlyUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<Role> roles = user.getRoles();
		
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		
		for (Role role : roles) {
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		}
		
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}
	
	public void setFirstName(String firstName) {
		this.user.setFirstName(firstName);
	} 
	
	public void setLastName(String lastName) {
		this.user.setLastName(lastName);
	} 
	
	public String getFullName() {
		return this.user.getFirstName() + " " + this.user.getLastName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

	public boolean hasRole(String roleName) {
		return user.hasRole(roleName);
	}
}
