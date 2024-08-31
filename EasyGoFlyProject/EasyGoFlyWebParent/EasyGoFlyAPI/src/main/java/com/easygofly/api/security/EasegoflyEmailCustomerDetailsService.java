package com.easygofly.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.entity.Customer;

public class EasegoflyEmailCustomerDetailsService implements UserDetailsService{
@Autowired	private CustomerRepository repo; 
	
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Customer customer = repo.getCustomerByEmail(email);
		
//		System.out.println(customer.getPhoneNumber());
		
		if(customer != null) {
			return new EasegoflyEmailCustomerDetails(customer);
		}

		throw new UsernameNotFoundException("Could not find user with email: " + email);
		
	}
}
