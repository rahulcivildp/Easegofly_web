package com.easygofly.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.entity.Customer;

public class EasegoflyPhoneCustomerDetailsService implements UserDetailsService{
	
	@Autowired	private CustomerRepository repo; 
	
	
	@Override
	public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
		Customer customer = repo.getCustomerByPhone(phone);
		
//		System.out.println(customer.getPhoneNumber());
		
		if(customer != null) {
			return new EasegoflyPhoneCustomerDetails(customer);
		} else {
			customer = repo.getCustomerByEmail(phone);
			
			if(customer != null) {
				return new EasegoflyPhoneCustomerDetails(customer);
			} 
		}

		throw new UsernameNotFoundException("Could not find user with phone: " + phone);
		
	}
}
