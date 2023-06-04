package com.easygofly.site.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Role;



@SuppressWarnings("serial")
@Component
@AutoConfiguration
public class EasyGoFlyCustomerDetails implements UserDetails {

	private Customer customer;
	
	public EasyGoFlyCustomerDetails(Customer customer) {
		this.customer = customer;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<Role> roles = customer.getRoles();
		
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		
		for (Role role : roles) {
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return customer.getPassword();
	}

	@Override
	public String getUsername() {
		return customer.getEmail();
	}
	
	public void setFirstName(String firstName) {
		this.customer.setFirstName(firstName);
	} 
	
	public void setLastName(String lastName) {
		this.customer.setLastName(lastName);
	} 
	
	public String getFullName() {
		return this.customer.getFirstName() + " " + this.customer.getLastName();
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
		return customer.isEnabled();
	}

	public boolean hasRole(String roleName) {
		return customer.hasRole(roleName);
	}
	
	public Customer getCustomer() {
		return this.customer;
	}
	
	public String getEmail() {
		return this.customer.getEmail();
	}
}
