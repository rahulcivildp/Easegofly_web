package com.easygofly.shoppingCart;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.Setting;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.customer.CustomerRepository;
import com.easygofly.site.order.OrderRepository;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.setting.PaymentSettingBag;
import com.easygofly.site.setting.SettingRepository;
//import com.easygofly.site.setting.SettingService;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.razorpay.FundAccount;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;


@DataJpaTest
//@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class CartItemRepositoryTest {

	@Autowired private CartItemRepository repo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private TestEntityManager entityManager;
	@Autowired private CustomerRepository customerRepo;
	@Autowired private OrderRepository orderRepo;
	@Autowired private SettingRepository settingRepo;
	//@Autowired private SettingService settingService;
	
	@Test
	public void testSaveItem() {
		Integer customerId = 6;
		Integer productDetailsId = 12;
		
		Customer customer = entityManager.find(Customer.class, customerId);
		ProductDetail productDetail = entityManager.find(ProductDetail.class, productDetailsId);
		
		CartItem newItem = new CartItem();
		newItem.setCustomer(customer);
		newItem.setProductDetail(productDetail);
		newItem.setQuantity(1);
		
		CartItem savedItem = repo.save(newItem);
		
		assertThat(savedItem).isNotNull();
		assertThat(savedItem.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testSaveItem2() {
		Integer customerId = 6;
		Integer productDetailsId = 12;
		
		Customer customer = entityManager.find(Customer.class, customerId);
		ProductDetail productDetail = entityManager.find(ProductDetail.class, productDetailsId);
		
		CartItem Item1 = new CartItem();
		Item1.setCustomer(customer);
		Item1.setProductDetail(productDetail);
		Item1.setQuantity(1);
		
		CartItem Item2 = new CartItem();
		Item2.setCustomer(new Customer(customerId));
		Item2.setProductDetail(new ProductDetail(14));
		Item2.setQuantity(3);
		
		Iterable<CartItem> savedItem = repo.saveAll(List.of(Item1, Item2));
		
		assertThat(savedItem).isNotNull();
		assertThat(savedItem).size().isGreaterThan(0);
	}
	
	@Test
	public void testFindByCustomer() {
		Integer customerId = 6;
		List<CartItem>listItems = repo.findByCustomer(new Customer(customerId));
		
		listItems.forEach(System.out::println);
		
		assertThat(listItems.size()).isEqualTo(3);
	}
	
	@Test
	public void testFindByCustomerAndProductDetail() { 
		Integer customerId = 6;
		Integer productDetailsId = 12;
		
		CartItem item = repo.findByCustomerAndProductDetail(new Customer(customerId), new ProductDetail(productDetailsId));
		
		assertThat(item).isNotNull();
		
		System.out.println(item);
	}
	
	@Test
	public void testUpdateQuantity() { 
		Integer customerId = 6;
		Integer productDetailsId = 12;
		//Integer quantity = 5;
		
		//repo.updateQuantity(quantity, customerId, productDetailsId);
		
		CartItem item = repo.findByCustomerAndProductDetail(new Customer(customerId), new ProductDetail(productDetailsId));
		
		assertThat(item.getQuantity()).isEqualTo(5);
	}
	
	@Test
	public void testDeleteQuantity() {
		Integer customerId = 6;
		Integer productDetailsId = 14;
		
		repo.deleteCustomerAndProductDetail(customerId, productDetailsId);
		
		CartItem item = repo.findByCustomerAndProductDetail(new Customer(customerId), new ProductDetail(productDetailsId));
		
		assertThat(item).isNull();
	}
	
	@Test
	public void updateCartHistory() {
		CartItem cartItem = repo.findById(21).get();
		List<SearchHistory> search = cartItem.getSearchHistory();
		for (SearchHistory searchHistory : search) {
			searchHistory.setCartItem(null);
			searchRepo.save(searchHistory);
		}
		
		cartItem.setSearchHistory(null);
		repo.save(cartItem);
		
		repo.deleteById(21);
		
	}
	
	@Test
	public void testCreateCustomer() throws RazorpayException {
		Customer getCustomer = customerRepo.findById(6).get();
		RazorpayClient razorpay = new RazorpayClient("rzp_test_p8o3jHNzUkOPTt", "hPofL5jy7hbg2f1uPWA8xKMe");

		JSONObject customerRequest = new JSONObject();
		customerRequest.put("name",getCustomer.getFullName());
		customerRequest.put("contact",getCustomer.getPhone());
		customerRequest.put("email",getCustomer.getEmail());
		customerRequest.put("fail_existing","0");
		//customerRequest.put("gstin","29XAbbA4369J1PA");
		JSONObject notes = new JSONObject();
		notes.put("notes_key_1","some customer");
		notes.put("notes_key_2","I don't know.");
		customerRequest.put("notes",notes);

		com.razorpay.Customer customer = razorpay.customers.create(customerRequest);
		
		System.out.println(customer);
	}
	
	@Test
	public void testCreateCustomer2() throws RazorpayException, IOException {
		RazorpayClient razorpay = new RazorpayClient("rzp_test_p8o3jHNzUkOPTt", "hPofL5jy7hbg2f1uPWA8xKMe");
		
		String cust = "cust_KuoN5TzkbZ8zD3";
		
		JSONObject customerRequest = new JSONObject();
		customerRequest.put("contact_id", cust);
		customerRequest.put("account_type", "bank_account");
		JSONObject bankAccount = new JSONObject();
		bankAccount.put("name", "Tanmay Sarkar");
		bankAccount.put("ifsc", "SBIN0013125");
		bankAccount.put("account_number", "35324656217");
		customerRequest.put("bank_account", bankAccount);
		
		FundAccount fundAccount = razorpay.fundAccount.create(customerRequest);
		
		System.out.println(fundAccount);
	}
	
	@Test
	public void testOrder() throws RazorpayException, IOException {
		URL url = new URL("https://api.razorpay.com/v1/contacts");
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
		http.disconnect();
	}
	
	@Test
	public void testPaymentRazorpay() throws RazorpayException {
		RazorpayClient razorpay = new RazorpayClient("rzp_test_p8o3jHNzUkOPTt", "hPofL5jy7hbg2f1uPWA8xKMe");

		String paymentId = "pay_29QQoUBi66xm2f";

		JSONObject paymentRequest = new JSONObject();
		paymentRequest.put("amount", 1000);
		paymentRequest.put("currency", "INR");
		        
		Payment payment = razorpay.payments.capture(paymentId, paymentRequest);

		System.out.println(payment);
	}
}
