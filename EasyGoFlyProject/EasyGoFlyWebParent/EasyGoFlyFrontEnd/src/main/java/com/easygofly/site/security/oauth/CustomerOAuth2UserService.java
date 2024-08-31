package com.easygofly.site.security.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;


@Service
public class CustomerOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired private CustomerService customerService;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String clientName = userRequest.getClientRegistration().getClientName();
		OAuth2User user = super.loadUser(userRequest);

		Customer customer = customerService.getByEmail(user.getAttribute("email"));
		
		return new CustomerOAuth2User(user, clientName, customer);
	}
	
}
