package com.easygofly.site.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.easygofly.entity.Customer;
import com.easygofly.site.customer.CustomerRepository;


public class EasyGoFlyCustomerDetailsService implements UserDetailsService {

	@Autowired
	private CustomerRepository repo; 
	
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Customer customer = repo.getCustomerByEmail(email);
		if(customer != null) {
			return new EasyGoFlyCustomerDetails(customer);
		}

		throw new UsernameNotFoundException("Could not find user with email: " + email);
		
	}

}
