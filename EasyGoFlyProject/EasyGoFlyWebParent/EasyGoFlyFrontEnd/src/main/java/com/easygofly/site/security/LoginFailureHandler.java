package com.easygofly.site.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerService;

@Component
@EntityScan({"com.easygofly.entity", "com.easygofly.site.customer"})
@Transactional
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	@Autowired private CustomerService customerService;
     
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
    	
	  String phone = request.getParameter("phone");
	  
      System.out.println("On Authentication Failure Phone: " + phone);
      request.setAttribute("phone", phone);
       
      String redirectURL = "/login?error&phone=" + phone;

      if (phone.contains("@")) {
          Customer customerCheck = customerService.getCustomerByEmail(phone);
          if (customerCheck == null) {
              try {
    			customerService.registerCustomerByEmail(phone);
    		} catch (UnsupportedEncodingException | MessagingException e) {
    			e.printStackTrace();
    		}
    	  } 
      } else {
    	  Customer customerCheck = customerService.getCustomerByPhone(phone);
          if (customerCheck == null) {
    		customerService.registerCustomerByPhone(phone);
    	  }
      }
       
      if (exception.getMessage().contains("OTP")) {
          redirectURL = "/login?otp=true&phone=" + phone;
      } else {
          Customer customer = customerService.getCustomerByPhone(phone);
          if (customer != null) {
        	  if (customer.isOTPRequired()) {
                  redirectURL = "/login?otp=true&phone=" + phone;
              } 
          }
          
          Customer customerEmail = customerService.getCustomerByEmail(phone);
          if (customerEmail != null) {
        	  if (customerEmail.isOTPRequired()) {
                  redirectURL = "/login?otp=true&phone=" + phone;
              } 
          }
          
      }
       
      super.setDefaultFailureUrl(redirectURL);       
      super.onAuthenticationFailure(request, response, exception);
      
      
    }
}
