package com.easygofly.site.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;

@Component
public class BeforeAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	@Autowired private CustomerService customerService;
     
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authManager) {
        super.setAuthenticationManager(authManager);
    }
     
    @Autowired
    @Override
    public void setAuthenticationFailureHandler(
            AuthenticationFailureHandler failureHandler) {
        super.setAuthenticationFailureHandler(failureHandler);
    }
     
    @Autowired
    public void setAuthenticationSuccessHandler(
    		LoginSuccessHandler successHandler) {
        super.setAuthenticationSuccessHandler(successHandler);
    }
 
    public BeforeAuthenticationFilter() {
        setUsernameParameter("phone"); 
        super.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/login", "POST"));
    }
     
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
                    throws AuthenticationException {

		String phone = request.getParameter("phone");

    	Customer customer = new Customer();

   	 	if (phone.contains("@")) {
   	 		
   	 		customer = customerService.getCustomerByEmail(phone);
   	 		
	   	 	if (customer != null) {
	    		if (customer.isOTPRequired()) {
	    			try {
			    		System.out.println("Attempt Authentication - Email: " + phone);
		        		return super.attemptAuthentication(request, response);
						
					} catch (Exception e) {
						customerService.clearOTP(customer);
//						e.printStackTrace();
					}
	        	}
	    		
	    		System.out.println("Attempt Authentication - Email: " + phone);
	    		float spamScore = getGoogleRecaptchaScore();
	    		
	    		if (spamScore < 0.5) {
	    			try {
	                    customerService.updateCustomerByEmail(customer);
	                    throw new InsufficientAuthenticationException("OTP");
	                } catch (Exception ex) {
	                    throw new AuthenticationServiceException(
	                                "Error while sending OTP on Email.");
	                }
	    		}
	    	}
		} else {

   	 		customer = customerService.getCustomerByPhone(phone);
   	 		
			if (customer != null) {
	    		if (customer.isOTPRequired()) {
		    		System.out.println("Attempt Authentication - Phone: " + phone);
	        		return super.attemptAuthentication(request, response);
	        	}
	    		
	    		System.out.println("Attempt Authentication - phone: " + phone);
	    		float spamScore = getGoogleRecaptchaScore();
	    		
	    		if (spamScore < 0.5) {
	    			try {
	                    customerService.updateCustomerByPhone(customer);
	                    throw new InsufficientAuthenticationException("OTP");
	                } catch (Exception ex) {
	                    throw new AuthenticationServiceException(
	                                "Error while sending OTP on Phone.");
	                }
	    		}
	    	}
		}
    	 
    	
    	
    	
		return super.attemptAuthentication(request, response);
    }
 
    private float getGoogleRecaptchaScore() {
        return 0.43f;
    }

}
