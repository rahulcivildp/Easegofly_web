package com.easygofly.site.security;

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

import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;

@Component
@EntityScan({"com.easygofly.entity", "com.easygofly.site.customer"})
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler{
		@Autowired private CustomerService customerService;
		public static final String REDIRECT_URL_SESSION_ATTRIBUTE_NAME = "REDIRECT_URL";
		private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		
		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication) throws ServletException, IOException {

	        System.out.println("onAuthentication Success");
	        
        	EasegoflyPhoneCustomerDetails customerDetails
            = (EasegoflyPhoneCustomerDetails) authentication.getPrincipal();
             
	        Customer customer = customerDetails.getCustomer();
			
			if (customer.getWallet() == null) {
				customerService.addWallet(customer);
			}
			
			customerService.clearOTP(customer);
			
			String targetUrl = determineTargetUrl(authentication, request);
			
			redirectStrategy.sendRedirect(request, response, targetUrl);
	        
		}
		
		protected String determineTargetUrl(final Authentication authentication, HttpServletRequest request) {
			Object redirectURLObject = request.getSession().getAttribute(REDIRECT_URL_SESSION_ATTRIBUTE_NAME);

			Map<String, String> roleTargetUrlMap = new HashMap<>();
			
	        if(redirectURLObject != null) {
	        	roleTargetUrlMap.put("Customer", redirectURLObject.toString());
	        } else{
	        	roleTargetUrlMap.put("Customer", "/");
	        }
	        
		    request.getSession().removeAttribute(REDIRECT_URL_SESSION_ATTRIBUTE_NAME);
		    
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
