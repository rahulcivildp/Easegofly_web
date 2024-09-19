package com.easygofly.site.security.oauth;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;


@Component
@AutoConfiguration
public class CustomerOAuth2User implements OAuth2User {

	private String clientName;
	private OAuth2User oauth2User;
	private Customer customer;
	
	public CustomerOAuth2User(OAuth2User oauth2User, String clientName, Customer customer) {
		this.oauth2User = oauth2User;
		this.clientName = clientName;
		this.customer = customer;
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
		return oauth2User.getAttribute("email");
	}

	public String getFullName() {
		return oauth2User.getAttribute("name");
	}

	public String getClientName() {
		return clientName;
	}

	public Customer getCustomer() {
		return this.customer;
	}

	public String getPhoto() {
		return this.customer.getPhotos();
	}
	
	public void setPhoto(String photo) {
		this.customer.setPhotos(photo);
	}
	
	public String getPhone() {
		return this.customer.getPhone();
	}

	public String getOpenid() {
		return oauth2User.getAttribute("sub"); 
	}
}
