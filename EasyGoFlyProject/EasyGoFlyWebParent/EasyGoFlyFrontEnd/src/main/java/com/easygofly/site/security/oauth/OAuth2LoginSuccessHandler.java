package com.easygofly.site.security.oauth;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.easygofly.entity.AuthenticationType;
import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;

@Component
@EntityScan({"com.easygofly.entity", "com.easygofly.site.customer"})
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler  {

	@Autowired private CustomerService customerService;
	
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		CustomerOAuth2User oauth2User = (CustomerOAuth2User) authentication.getPrincipal();
		
		String name = oauth2User.getName();
		String email = oauth2User.getEmail();
		String countryCode = request.getLocale().getCountry();
		String clientName = oauth2User.getClientName();
		System.out.println("name: " + name + "email: " + email + "countryCode: " + countryCode + "clientName: " + clientName );
		AuthenticationType authenticationType = getAuthenticationType(clientName);
		
		Customer customer = customerService.getByEmail(email);
		
		if (customer == null) {
			customerService.addNewCustomerUponOAuth2Login(name, email, countryCode, authenticationType); 
		} else {
			customerService.updateAuthentication(customer,authenticationType);
		}
		
		String targetUrl = determineTargetUrl(authentication);
		
		redirectStrategy.sendRedirect(request, response, targetUrl);
	}

	private AuthenticationType getAuthenticationType(String clientName) {
		if (clientName.equals("Google")) {
			return AuthenticationType.GOOGLE;
		} else if (clientName.equals("Facebook")) {
			return AuthenticationType.FACEBOOK;
		} else {
			return AuthenticationType.DATABASE;
		}
	}
	
	protected String determineTargetUrl(final Authentication authentication) {

	    Map<String, String> roleTargetUrlMap = new HashMap<>();
	    roleTargetUrlMap.put("ROLE_USER", "/");

	    final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
	    for (final GrantedAuthority grantedAuthority : authorities) {
	        String authorityName = grantedAuthority.getAuthority();
	        if(roleTargetUrlMap.containsKey(authorityName)) {
	            return roleTargetUrlMap.get(authorityName);
	        }
	    }

	    throw new IllegalStateException();
	}
}
