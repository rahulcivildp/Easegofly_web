package com.easygofly.site.customer;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.easygofly.entity.AuthenticationType;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Role;
import com.easygofly.entity.Wallet;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.Utility;
import com.easygofly.site.setting.CountryRepository;
import com.easygofly.site.setting.EmailSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.wallet.WalletRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import net.bytebuddy.utility.RandomString;

@Service
@Transactional
public class CustomerService {
	
	@Autowired private CustomerRepository customerRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private RoleRepository roleRepo;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private WalletRepository walletRepo;
	@Autowired private SettingService settingService;
	
	
	public List<Country> listAllCountries() {
		return countryRepo.findAllByOrderByNameAsc();
	}
	
//	public Customer getByEmail(String email) {
//		return customerRepo.getCustomerByEmail(email);
//	}
	
	public Customer getByPhone(String phone) {
		return customerRepo.getCustomerByPhone(phone);
	}
	
	public List<Role> listRoles() {
		return (List<Role>) roleRepo.findAll();
	}
	
	public Customer saveCustomer(Customer customer) {
		Role roleCustomer = new Role(3);
		customer.addRole(roleCustomer);
		
		boolean isUpdatingUser = (customer.getId() != null);
		
		if (isUpdatingUser) {
			Customer existingUser = customerRepo.findById(customer.getId()).get();
			if (customer.getPassword().isEmpty()) {
				customer.setPassword(existingUser.getPassword());
			} else {
				registerCustomer(customer);
			}
		} else {
			registerCustomer(customer);
		}
		return customerRepo.save(customer);
	}
	
	public Customer updateAccount(Customer customerInForm) {
		Customer userInDB = customerRepo.findById(customerInForm.getId()).get();
		
		if (!customerInForm.getPassword().isEmpty()) {
			customerInForm.setPassword(customerInForm.getPassword());
			registerCustomer(userInDB);
		} 
		
		if (customerInForm.getPhotos() != null) {
			userInDB.setPhotos(customerInForm.getPhotos());
		}
		
		userInDB.setFirstName(customerInForm.getFirstName());
		userInDB.setLastName(customerInForm.getLastName());
		userInDB.setPhone(customerInForm.getPhone());
		userInDB.setCity(customerInForm.getCity());
		userInDB.setState(customerInForm.getState());
		userInDB.setPostalCode(customerInForm.getPostalCode());
		userInDB.setAddressLine1(customerInForm.getAddressLine1());
		userInDB.setAddressLine2(customerInForm.getAddressLine2());
		userInDB.setResetPasswordToken(customerInForm.getResetPasswordToken());
		
		return customerRepo.save(userInDB);
	}
	
	private void registerCustomer(Customer customer) {
		encodePassword(customer);
		customer.setEnabled(false);
		customer.setCreatedTime(new Date());
		customer.setAuthenticationType(AuthenticationType.DATABASE);
		
		String randomCode = RandomString.make(64);
		customer.setVerificationCode(randomCode);
		
		customerRepo.save(customer);
		System.out.println("Verification Code: " + randomCode);
	}
	
	public void registerCustomerByPhone(String phone) {
			Customer newCust = new Customer();
			
			Role roleCustomer = new Role(3);
			newCust.addRole(roleCustomer);
			
			Country cntry = countryRepo.findById(106).get();
			newCust.setCountry(cntry);
	
			newCust.setEnabled(true);
			newCust.setEmail(phone + "@email.com");
			newCust.setFirstName(phone);
			newCust.setLastName(" ");
			newCust.setCreatedTime(new Date());
			newCust.setPhone(phone);
			newCust.setAuthenticationType(AuthenticationType.PHONE);
			newCust.setOtpRequestedTime(new Date());
			
			Random rnd = new Random();
			int data1 = rnd.nextInt(9); 
			int data2 = rnd.nextInt(9);
			int data3 = rnd.nextInt(9);
			int data4 = rnd.nextInt(9);
			int data5 = rnd.nextInt(9);
			int data6 = rnd.nextInt(9); 
			
			String randomCode = Integer.toString(data1) + Integer.toString(data2) + Integer.toString(data3) + Integer.toString(data4) + Integer.toString(data5) + Integer.toString(data6); 
			String encodeRandomeCode = passwordEncoder.encode(randomCode);
			newCust.setVerificationCode(encodeRandomeCode);
			
	//		String encodedPass = passwordEncoder.encode(randomCode);
			newCust.setPassword(encodeRandomeCode);
			System.out.println("New Customer " + encodeRandomeCode);
			
			customerRepo.save(newCust);
			
			sendOtpInPhone(newCust, randomCode);
			
			System.out.println("New Customer " + encodeRandomeCode);
			System.out.println("OTP Code: " + randomCode);
	
		
	}

	public void registerCustomerByEmail(String email) throws UnsupportedEncodingException, MessagingException{
		
			Customer newCust = new Customer();
		
			Role roleCustomer = new Role(3);
			newCust.addRole(roleCustomer);
			
			Country cntry = countryRepo.findById(106).get();
			newCust.setCountry(cntry);

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
			String encodeRandomeCode = passwordEncoder.encode(randomCode);
			newCust.setVerificationCode(encodeRandomeCode);

			System.out.println("Verification Code: 6666666666666666666666" + newCust.getId());
//			String encodedPass = passwordEncoder.encode(randomCode);
			newCust.setPassword(encodeRandomeCode);
			
			customerRepo.save(newCust);
			
			sendOtpInEmail(newCust, randomCode);
			
			System.out.println("Verification Code: " + encodeRandomeCode);
			System.out.println("OTP Code: " + randomCode);
			
			System.out.println("New Customer");
		
	}
	
	public void updateCustomerByEmail(Customer customer) throws UnsupportedEncodingException, MessagingException{
		customer.setAuthenticationType(AuthenticationType.EMAIL);
	    customer.setOtpRequestedTime(new Date());

		Random rnd = new Random();
		int data1 = rnd.nextInt(9); 
		int data2 = rnd.nextInt(9);
		int data3 = rnd.nextInt(9);
		int data4 = rnd.nextInt(9);
		int data5 = rnd.nextInt(9);
		int data6 = rnd.nextInt(9); 
		
		String randomCode = Integer.toString(data1) + Integer.toString(data2) + Integer.toString(data3) + Integer.toString(data4) + Integer.toString(data5) + Integer.toString(data6); 
		String encodeRandomeCode = passwordEncoder.encode(randomCode);
		customer.setVerificationCode(encodeRandomeCode);

//		String encodedPass = passwordEncoder.encode(randomCode);
		customer.setPassword(encodeRandomeCode);
		
		customerRepo.save(customer);
		
		sendOtpInEmail(customer, randomCode);
		
		System.out.println("Verification Code: " + encodeRandomeCode);
		System.out.println("OTP Code: " + randomCode);
	}
	
	public void updateCustomerByPhone(Customer customer) throws UnsupportedEncodingException, MessagingException{

		customer.setAuthenticationType(AuthenticationType.PHONE);
	    customer.setOtpRequestedTime(new Date());
		
		Random rnd = new Random();
		int data1 = rnd.nextInt(9); 
		int data2 = rnd.nextInt(9);
		int data3 = rnd.nextInt(9);
		int data4 = rnd.nextInt(9);
		int data5 = rnd.nextInt(9);
		int data6 = rnd.nextInt(9); 
		
		String randomCode = Integer.toString(data1) + Integer.toString(data2) + Integer.toString(data3) + Integer.toString(data4) + Integer.toString(data5) + Integer.toString(data6); 
		String encodeRandomeCode = passwordEncoder.encode(randomCode);
		customer.setVerificationCode(encodeRandomeCode);
		
//		String encodedPass = passwordEncoder.encode(randomCode);
		customer.setPassword(encodeRandomeCode);
		
		customerRepo.save(customer);
		
		sendOtpInPhone(customer, randomCode);
		
		System.out.println("Verification Code: " + encodeRandomeCode);
		System.out.println("OTP Code: " + randomCode);
	}
	
	private void encodePassword(Customer customer) {
		String encodedPass = passwordEncoder.encode(customer.getPassword());
		System.out.println("encodedPassword: " + encodedPass);
		customer.setPassword(encodedPass);
	}

	public boolean verify(String verificationCode) {
		Customer customer = customerRepo.getCustomerByVerificationCode(verificationCode);
		
		if (customer == null || customer.isEnabled()) {
			return false;
		} else {
			customerRepo.updateEnableStatus(customer.getId());
			return true;
		}
	}

	public boolean verifyByOTP(String verificationCode, String phoneNum) {
		Customer customer = customerRepo.getCustomerByVerificationCodePhone(verificationCode, phoneNum);
		
		System.out.println(customer.getEmail() + "6666666666666666666666666");
		
		if (customer == null || customer.isEnabled()) {
			return false;
		} else {
			customerRepo.updateEnableStatus(customer.getId());
			return true;
		}
	}
	
	public void sendOtpInPhone (Customer customer, String otp) {
		
		String ACCOUNT_SID = "AC2ff01adb5a1076761844cbea16411c7f";
		String AUTH_TOKEN = "bae802cb2278738425766f5d0e9cf752";
		String TWILIO_PHONE_NUMBER = "+15166897173";
		
		 Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		 
	
	        // Destination phone number (recipient)
	        String toPhoneNumber = "+91" + customer.getPhone(); // Replace with the recipient's phone number
	
	        // Message body
	        String messageBody = "Your OTP is: " + otp;
	        
	        Message message = Message.creator(
	                new PhoneNumber(toPhoneNumber),  // To phone number
	                new PhoneNumber(TWILIO_PHONE_NUMBER),  // From Twilio phone number
	                messageBody)
	            .create();
	
	        System.out.println("OTP sent successfully. SID: " + message.getSid());
		    
	}

	public void sendOtpInEmail (Customer customer, String otp) throws UnsupportedEncodingException, MessagingException {
		
		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
		
		String toAddress = customer.getEmail();
		String subject = emailSettings.getCunstomerVerifySubject();
		String content = emailSettings.getCunstomerVerifyContent();
		
		MimeMessage message= mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		content = content.replace("[[name]]", customer.getFullName());
		
		content = content.replace("[[OTP]]", otp);
		
		helper.setText(content, true);
		
		mailSender.send(message);
		
		System.out.println("To Address: " + toAddress);
		System.out.println("Verify OTP: " + otp);
		    
	}
	
	public void clearOTP(Customer customer) {
	    customer.setVerificationCode(null);
	    customer.setOtpRequestedTime(null);
	    customerRepo.save(customer);
	}
	
	public void updateAuthentication(Customer customer, AuthenticationType type) {
		if (!customer.getAuthenticationType().equals(type)) {
			customerRepo.updateAuthenticationType(customer.getId(), type);
		} 
	}
	
	public Customer addWallet(Customer customer) {
		Wallet wallet = new Wallet();
		wallet.setBalance(0);
		wallet.setCustomer(customer);
		wallet.setTempValue(0);
		
		Wallet savedWallet = walletRepo.save(wallet);
		customer.setWallet(savedWallet);
		return customerRepo.save(customer);
	}
	
	public Customer getCustomerByEmail(String email) {
		return customerRepo.findCustomerByEmail(email);
	}
	
	public Customer getCustomerByPhone(String phone) {
		return customerRepo.findCustomerByPhone(phone);
	}
	
	public Customer addNewCustomerUponOAuth2Login(String name, String email, String countryCode, AuthenticationType authenticationType) {
		Role roleCustomer = new Role(3);
		Customer customer = new Customer();
		customer.setEmail(email);
		
		setNameSplit(name, customer);
		
		customer.setEnabled(true);
		customer.addRole(roleCustomer);
		customer.setCreatedTime(new Date());
		customer.setAuthenticationType(authenticationType);
		customer.setPassword("");
		customer.setAddressLine1("");
		customer.setCity(""); 
		customer.setState("");
		customer.setCountry(countryRepo.findByCode(countryCode));
		customer.setPostalCode("");
		customer.setPhone("");
		
		return customerRepo.save(customer);
	}
	
	private void setNameSplit(String name, Customer customer) {
		String[] nameArray = name.split(" ");
		if (nameArray.length < 2) {
			customer.setFirstName(name);
			customer.setLastName("");
		} else {
			customer.setFirstName(nameArray[0]);
			
			String lastName = name.replaceFirst(nameArray[0] + " ", "");
			customer.setLastName(lastName);
		}
	}
	
	public boolean forgotPass(String token) {
		Customer customer = customerRepo.getCustomerByResetPasswordToken(token);
		
		if (customer == null) {
			System.out.println("returned false");
			return false;
		} else {
			System.out.println("returned true" + customer.getResetPasswordToken());
			return true;
		}
	}
	
	public boolean isEmailUnique(Integer id, String email) {
		Customer userByEmail = customerRepo.getCustomerByEmail(email);
		
		if (userByEmail == null) return true;
		
		boolean isCreatingNew = (id == null);
		
		if (isCreatingNew) {
			if (userByEmail != null) return false;
		} else {
			if (userByEmail.getId() != id) {
				return false;
			}
		}
		
		return true;
	}

	public Customer findByCode(String code) {
		Customer existingCustomer = customerRepo.getCustomerByVerificationCode(code);
		return existingCustomer;
	}
	
	public Customer findByToken(String token) {
		Customer existingCustomer = customerRepo.getCustomerByResetPasswordToken(token);
		return existingCustomer;
	}
	
	public Customer updateCustomer(Integer id) throws UserNotFoundException {
		
		try {
			return customerRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new UserNotFoundException("Could not find any user with ID: " + id);
		}
	}
	
	public void deleteCustomer(Integer id) throws UserNotFoundException {
		Long count = customerRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any user with ID: " + id);
		}
		
		customerRepo.deleteById(id);
	}
	
	public Customer passwordSave(Customer customerInForm, String password) {

		customerInForm.setPassword(passwordEncoder.encode(password));
		customerInForm.setResetPasswordToken(null);
		System.out.println("Show password: " + customerInForm.getPassword());
		return customerRepo.save(customerInForm);
	}

	public String updateResetPasswordToekn(String email) throws UserNotFoundException {
		Customer customer = customerRepo.findCustomerByEmail(email); 
		if (customer != null) {
			String token = RandomString.make(30);
			customer.setResetPasswordToken(token);
			customerRepo.save(customer);
			return token;
		} else {
			throw new UserNotFoundException("Could not find by this Email ID: "+ email +". Please register to proceed to your account.");
		}
		
	}
}
