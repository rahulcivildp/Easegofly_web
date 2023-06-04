package com.easygofly.admin.customer;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.easygofly.admin.order.CartItemRepository;
import com.easygofly.admin.order.OrderRepository;
import com.easygofly.admin.setting.CountryRepository;
import com.easygofly.admin.user.RoleRepository;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.RechargeHistoryStatus;
import com.easygofly.entity.Role;
import com.easygofly.entity.Wallet;
import com.easygofly.entity.exception.UserNotFoundException;

import net.bytebuddy.utility.RandomString;

@Service
@Transactional
public class CustomerService {
	public static final int CUSTOMER_PER_PAGE = 6;
	
	@Autowired private CustomerRepository customerRepo;
	@Autowired private RoleRepository roleRepo;
	@Autowired private CountryRepository countryRepo;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private CartItemRepository cartItemRepo;
	@Autowired private OrderRepository orderRepo;
	@Autowired private RechargeHistoryRepository rechargeHistoryRepo ;
	@Autowired private WalletRepository walletRepo  ;
	
	
	public List<Country> listAllCountries() {
		return countryRepo.findAllByOrderByNameAsc();
	}
	
	public Customer getByEmail(String email) {
		return customerRepo.getCustomerByEmail(email);
	}
	
	public List<Customer> listAll() {
		return (List<Customer>) customerRepo.findAll(Sort.by("firstName").ascending());
	}
	
	public Page<Customer> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, CUSTOMER_PER_PAGE, sort);
		
		if(keyword != null) {
			return customerRepo.findUser(keyword, pageable);
		}
		return customerRepo.findAll(pageable);
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

	private void registerCustomer(Customer customer) {
		encodePassword(customer);
		customer.setEnabled(false);
		customer.setCreatedTime(new Date());
		
		String randomCode = RandomString.make(64);
		customer.setVerificationCode(randomCode);
		
		customerRepo.save(customer);
		System.out.println("Verification Code: " + randomCode);
	}
	
	private void encodePassword(Customer customer) {
		String encodedPass = passwordEncoder.encode(customer.getPassword());
		customer.setPassword(encodedPass);
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
		
		return customerRepo.save(userInDB);
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

	public Customer updateCustomer(Integer id) throws UserNotFoundException {
		
		try {
			return customerRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new UserNotFoundException("Could not find any user with ID: " + id);
		}
	}
	
	public void deleteCustomer(Integer id) throws UserNotFoundException {
		Customer customer = customerRepo.findById(id).get();
		List<CartItem> cartItems = cartItemRepo.findByCustomer(customer);
		List<Order> orders = orderRepo.findByCustomer(customer);
		if (orders != null) {
			for (Order order : orders) {
				order.setCustomer(null);
				orderRepo.save(order);
			}
		}
		if (cartItems != null) {
			for (CartItem cartItem : cartItems) {
				cartItem.setCustomer(null);
				cartItemRepo.save(cartItem);
			}
		}
		
		Long count = customerRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any user with ID: " + id);
		}
		
		customerRepo.deleteById(id);
	}
	
	public void updateCustomerEnabledStatus(Integer id, boolean enabled) {
		customerRepo.updateEnableStatus(id, enabled);
	}
	
	public RechargeHistory createRechargeHistory(Customer customer, String transId, Integer addedBalance) {
		Wallet wallet = customer.getWallet();
		
		RechargeHistory rechargeHistory = new RechargeHistory();
		rechargeHistory.setWallet(wallet);
		rechargeHistory.setRechargeAmount(addedBalance);
		rechargeHistory.setTransaction(transId);
		rechargeHistory.setDate(new Date());
		rechargeHistory.setRechargeHistoryStatus(RechargeHistoryStatus.SUCCESSFULL);

		return rechargeHistoryRepo.save(rechargeHistory);
	}
	
	public List<RechargeHistory> listAllRechargeHistory(Wallet wallet, Sort sort) {
		List<RechargeHistory> rechargeHistories = rechargeHistoryRepo.findByWallet(wallet, sort);
		return rechargeHistories;
	}
	
	public Wallet createWallet(Customer customer) {
		Wallet wallet = new Wallet();
		
		wallet.setCustomer(customer);
		wallet.setBalance(0);
		wallet.setTempValue(0);

		return walletRepo.save(wallet);
	}
}
