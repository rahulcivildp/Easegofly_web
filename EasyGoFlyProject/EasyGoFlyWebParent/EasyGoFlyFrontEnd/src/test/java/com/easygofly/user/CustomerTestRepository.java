package com.easygofly.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.Customer;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.customer.CustomerRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class CustomerTestRepository {

	@Autowired private CustomerRepository customerRepo;
	@Autowired private static PasswordEncoder passwordEncoder;
	
	@Test
	public void findByEmail() {
		String email = "info.aaladin@gmail.com";
		Customer existingCustomer = customerRepo.findCustomerByEmail(email);
		
		if (existingCustomer.getEmail() == null) {
			System.out.println("Ok to continue!" + existingCustomer.getEmail());
		} else {
			System.out.println("Duplicate Email found!");
		}
	}
	
	@Test
	public void testUpdateAuthenticationType() {
		String encodedPass = passwordEncoder.encode("12345678");
		
		System.out.println("Encoded password: " + encodedPass);
	}
	
	
}
