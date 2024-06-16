package com.easygofly.site.shoppingCart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.order.OrderService;
import com.easygofly.site.flight.SearchHistoryRepository;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.PaymentSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class CartItemController {
	
	@Autowired private CartItemService cartService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private OrderService orderService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private CustomerService customerService;
	@Autowired private SettingService settingService;
	

	@GetMapping("/manage_booking")
	public String getManageBooking(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model, HttpServletRequest request) {
		return listByPage(loggedCustomer, googleLogin, 1, model, "id", "asc", null, request);
	}
	
	@GetMapping("/manage_booking/{pageNum}")
	public String listByPage(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, @PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword, HttpServletRequest request) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			pagingCartItem(pageNum, sortField, sortDir, keyword, customer, model);
			pagingOrder(1, sortField, sortDir, customer, model, request);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			pagingCartItem(pageNum, sortField, sortDir, keyword, customer, model);
			pagingOrder(1, sortField, sortDir, customer, model, request);
		}
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		
		return "cart/manage_booking";
	} 
	
	@GetMapping("/manage_orders/{pageNum}")
	public String listByPageOrder(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, @PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword, HttpServletRequest request) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			pagingCartItem(1, sortField, sortDir, keyword, customer, model);
			pagingOrder(pageNum, sortField, sortDir, customer, model, request);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			pagingCartItem(1, sortField, sortDir, keyword, customer, model);
			pagingOrder(pageNum, sortField, sortDir, customer, model, request);
		}
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		
		return "cart/manage_order";
	} 
	
	private Page<Order> pagingOrder(int pageNum, String sortField, String sortDir, Customer customer, Model model, HttpServletRequest request) {
		Page<Order> pageOrder = orderService.listByPageOrder(customer, pageNum, sortField, sortDir);
    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		
		List<Order> listOrders = pageOrder.getContent();

		OrderStatus newOrder = OrderStatus.NEW;
		OrderStatus orderCancelled = OrderStatus.CANCELLED;
		OrderStatus orderSuccess = OrderStatus.SUCCESSFULL;
		OrderStatus orderFailed = OrderStatus.FAILED;
		OrderStatus orderPending = OrderStatus.PENDING;
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
	    
	    for (Cookie cookie : request.getCookies()) {
			if(cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}
	    
		for (Order order : listOrders) {
			if (order.getOrderStatus() != orderSuccess) {
				ProductDetail productDetail = order.getProductDetail();
				model.addAttribute("totalSeat", Integer.parseInt(productDetail.getTotalSeats()));
				model.addAttribute("passengerNum", order.getPassengerNum());
				
				String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
				Integer intAmount = (int) (order.getPrice() * 100);
				String amount = "" + intAmount;
				//String amount = "100";
	
				//Cookie cookie = request.getCookies().get("JSESSIONID");
				//String value = cookie.getValue();
	
				Transaction transaction = new Transaction();
				
				try {
					ZaakpayApiRequestParameters processPayment = transaction.processPayment(orderString, amount, paymentSettingBag);
					
					model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
					model.addAttribute("requestUrl", processPayment.getRequestUrl());
					model.addAttribute("checksum", processPayment.getChecksum());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(order.getId());
			}
		}
		/*===================================*/
		
		long startCount1 = (pageNum - 1) * OrderService.ORDER_PER_PAGE + 1;
		long endCount1 = startCount1 + OrderService.ORDER_PER_PAGE - 1;
		if (endCount1 > pageOrder.getTotalElements()) {
			endCount1 = pageOrder.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages1", pageOrder.getTotalPages());
		model.addAttribute("totalItems1", pageOrder.getTotalElements());
		model.addAttribute("startCount1", startCount1);
		model.addAttribute("endCount1", endCount1);
		model.addAttribute("listOrders", listOrders);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("newOrder", newOrder);
		model.addAttribute("orderCancelled", orderCancelled);
		model.addAttribute("orderSuccess", orderSuccess);
		model.addAttribute("orderFailed", orderFailed);
		model.addAttribute("orderPending", orderPending);
		model.addAttribute("reverseSort", reverseSort);
		
		return pageOrder;
	}

	private Page<CartItem> pagingCartItem(int pageNum, String sortField, String sortDir, String keyword,
			Customer customer, Model model) {
		Page<CartItem> pageCart = cartService.listByPage(pageNum, sortField, sortDir, keyword, customer);
		
		List<CartItem> listItems = pageCart.getContent();
		for (CartItem cartItem : listItems) {
			if (!cartItem.equals(null)) {
				List<SearchHistory> findByItem = searchRepo.findByCartItem(cartItem);
				for (SearchHistory history : findByItem) {
					model.addAttribute("history", history);
				}
			} 
		}
		
		long startCount = (pageNum - 1) * CartItemService.ITEM_PER_PAGE + 1;
		long endCount = startCount + CartItemService.ITEM_PER_PAGE - 1;
		if (endCount > pageCart.getTotalElements()) {
			endCount = pageCart.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageCart.getTotalPages());
		model.addAttribute("totalItems", pageCart.getTotalElements());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("listItems", listItems);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		
		return pageCart;
	} 
	
	@GetMapping("/delete_booking_{id}")
	public String deleteCartItem(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			CartItem cartItem = cartRepo.findById(id).get();
			List<SearchHistory> search = cartItem.getSearchHistory();
			for (SearchHistory searchHistory : search) {
				searchHistory.setCartItem(null);
				searchRepo.save(searchHistory);
			}
			cartItem.setSearchHistory(null);
			cartRepo.save(cartItem);
			
			cartService.deleteCartItem(id);
			redirectAttributes.addFlashAttribute("warning", "Cart Item is deleted successfully.");
			
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/manage_booking#flightCart";
	}
}
