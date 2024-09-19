package com.easygofly.api.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.easygofly.api.customer.CustomerService;
import com.easygofly.entity.AuthenticationType;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    @Autowired private CustomerService customerService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, ApplicationContext ctx) {
        this.authenticationManager = authenticationManager;
        this.customerService = ctx.getBean(CustomerService.class);
        setFilterProcessesUrl("/api/login");
    }
    
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
            UserCredentials creds = new ObjectMapper().readValue(request.getInputStream(), UserCredentials.class);
            if (creds.getAuthentication_type().equals("GOOGLE")) {
            	customerService.updateOpenid(creds.getUsername(), creds.getOpen_id(), AuthenticationType.GOOGLE);
            	creds.setPassword(creds.getOpen_id());
                return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                creds.getUsername(),
                                creds.getPassword(),
                                Collections.emptyList()
                        )
                );
			} else {
                return authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                creds.getUsername(),
                                creds.getPassword(),
                                Collections.emptyList()
                        )
                );
			}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	@Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {

    	EasegoflyPhoneCustomerDetails customerDetails
        = (EasegoflyPhoneCustomerDetails) authResult.getPrincipal();

        Customer customer = customerDetails.getCustomer();
        Country country = customer.getCountry();
        
        String token = JwtUtil.generateToken(authResult);
        response.addHeader("Authorization", "Bearer " + token);
        response.setContentType("application/json");
        int type = 0;
        
        if (customer.getAuthenticationType().equals(AuthenticationType.GOOGLE)) {
        	type = 2;
	    } else if (customer.getAuthenticationType().equals(AuthenticationType.FACEBOOK)) {
	        type = 3;
		} else if (customer.getAuthenticationType().equals(AuthenticationType.DATABASE)) {
	        type = 1;
		}
        
        String customerBody =  "{"
        		+ "\"id\": " + customer.getId() + ", "
                + "\"token\": \"Bearer " + token + "\", "
        		+ "\"email\": \"" + customer.getEmail() + "\", "
        		+ "\"phone\": \"" + customer.getPhone() + "\", "
        		+ "\"firstName\": \"" + customer.getFirstName() + "\", "
        		+ "\"lastName\": \"" + customer.getLastName() + "\", "
                + "\"name\": \"" + customer.getFirstName() + " " + customer.getLastName() + "\", "
        		+ "\"gender\": \"" + customer.getGender() + "\", "
        		+ "\"photo\": \"" + customer.getPhotosImagePath() + "\", "
                + "\"avatar\": \"" + customer.getPhotosImagePath() + "\", "
        		+ "\"address\": \"" + customer.getAddressLine1() + "\", "
        		+ "\"addressAdditional\": \"" + customer.getAddressLine2() + "\", "
        		+ "\"pin\": \"" + customer.getPostalCode() + "\", "
        		+ "\"state\": \"" + customer.getState() + "\", "
        		+ "\"city\": \"" + customer.getCity() + "\", "
        		+ "\"country\": \"" + country.getName() + "\", "
                + "\"description\": \"\", "
                + "\"auth_type\": \"" + customer.getAuthenticationType() + "\", "
                + "\"type\": " + type + ", "
        		+ "\"createdTime\": \"" + customer.getCreatedTime() + "\", "
                + "\"wallet_id\": " + customer.getWallet().getId() + " "
        		+ "}";

        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Login Successful.\", "
        		+ "\"data\": " + customerBody + ", "
        		+ "\"wallet_id\": " + customer.getWallet().getId() + " "
        		+ "}";

        PrintWriter out = response.getWriter();
        JsonObject jsonObj = JsonParser.parseString(responseBody).getAsJsonObject();
        out.print(jsonObj);
        out.flush();
    }

    private static class UserCredentials {
        private String username;
        private String password;
        private String authentication_type;
        private String open_id;
        
		public String getUsername() {
			return username;
		}
		@SuppressWarnings("unused")
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		@SuppressWarnings("unused")
		public void setPassword(String password) {
			this.password = password;
		}
		public String getAuthentication_type() {
			return authentication_type;
		}
		@SuppressWarnings("unused")
		public void setAuthentication_type(String authentication_type) {
			this.authentication_type = authentication_type;
		}
		public String getOpen_id() {
			return open_id;
		}
		@SuppressWarnings("unused")
		public void setOpen_id(String open_id) {
			this.open_id = open_id;
		}

        
    }

}
