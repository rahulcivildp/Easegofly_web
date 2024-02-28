package com.easygofly.user;

import java.util.Date;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.AuthenticationType;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Role;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.customer.CustomerRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class CustomerTestRepository {

	@Autowired private CustomerRepository customerRepo;
	private PasswordEncoder passwordEncoder;
	
	@Test
	public void findByEmail() {
		String email = "info.aalassdin@gmail.com";
		Customer existingCustomer = customerRepo.findCustomerByEmail(email);
		
		if (existingCustomer.getEmail() != null) {
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
	
	@Test
	public void sendSms() {
		String ACCOUNT_SID = "AC2ff01adb5a1076761844cbea16411c7f";
		String AUTH_TOKEN = "bae802cb2278738425766f5d0e9cf752";
		String TWILIO_PHONE_NUMBER = "+15166897173";
		
		 Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		 
		  // Generate a random OTP (replace this with your OTP generation logic)
	        String otp = "123456";

	        // Destination phone number (recipient)
	        String toPhoneNumber = "+917548003641"; // Replace with the recipient's phone number

	        // Message body
	        String messageBody = "Your OTP is: " + otp;
	        
	        Message message = Message.creator(
	                new PhoneNumber(toPhoneNumber),  // To phone number
	                new PhoneNumber(TWILIO_PHONE_NUMBER),  // From Twilio phone number
	                messageBody)
	            .create();

	        System.out.println("OTP sent successfully. SID: " + message.getSid());
	    }

	@Test
	public void testFindUser() {
		String email = "rafssddfdf@gmail.com";
		Customer newCust = new Customer();
		
		Role roleCustomer = new Role(3);
		newCust.addRole(roleCustomer);

		newCust.setEnabled(true);
		newCust.setFirstName(email);
		newCust.setLastName(" ");
		newCust.setCreatedTime(new Date());
		newCust.setAuthenticationType(AuthenticationType.EMAIL);
		newCust.setOtpRequestedTime(new Date());
		newCust.setPhone(email);
		newCust.setEmail(email);

		Random rnd = new Random();
		int data1 = rnd.nextInt(9); 
		int data2 = rnd.nextInt(9);
		int data3 = rnd.nextInt(9);
		int data4 = rnd.nextInt(9);
		int data5 = rnd.nextInt(9);
		int data6 = rnd.nextInt(9); 
		
		String randomCode = Integer.toString(data1) + Integer.toString(data2) + Integer.toString(data3) + Integer.toString(data4) + Integer.toString(data5) + Integer.toString(data6); 
//		String encodeRandomeCode = passwordEncoder.encode(randomCode);
		newCust.setVerificationCode(randomCode);

//		String encodedPass = passwordEncoder.encode(randomCode);
		newCust.setPassword(randomCode);
		System.out.println("Verification Code: ");

		System.out.println("Verification Code: 11111111111111111111111" + randomCode);
		
		customerRepo.save(newCust);
		
//		System.out.println("Verification Code: " + encodeRandomeCode);
		System.out.println("OTP Code: " + randomCode);
		
		System.out.println("New Customer");
	}
}
