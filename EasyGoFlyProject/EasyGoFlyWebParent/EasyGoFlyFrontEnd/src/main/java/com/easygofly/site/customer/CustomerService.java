package com.easygofly.site.customer;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.easygofly.entity.AuthenticationType;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Role;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.setting.CountryRepository;

import net.bytebuddy.utility.RandomString;

@Service
@Transactional
public class CustomerService {
	
	@Autowired private CustomerRepository customerRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private RoleRepository roleRepo;
	@Autowired private PasswordEncoder passwordEncoder;
	
	
	public List<Country> listAllCountries() {
		return countryRepo.findAllByOrderByNameAsc();
	}
	
	public Customer getByEmail(String email) {
		return customerRepo.getCustomerByEmail(email);
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
		userInDB.setPhoneNumber(customerInForm.getPhoneNumber());
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
	
	public void updateAuthentication(Customer customer, AuthenticationType type) {
		if (!customer.getAuthenticationType().equals(type)) {
			customerRepo.updateAuthenticationType(customer.getId(), type);
		} 
	}
	
	public Customer getCustomerByEmail(String email) {
		return customerRepo.findCustomerByEmail(email);
	}
	
	public void addNewCustomerUponOAuth2Login(String name, String email, String countryCode, AuthenticationType authenticationType) {
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
		customer.setPhoneNumber("");
		
		customerRepo.save(customer);
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
