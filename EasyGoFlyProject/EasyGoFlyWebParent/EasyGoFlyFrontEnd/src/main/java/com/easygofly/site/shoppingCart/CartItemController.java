package com.easygofly.site.shoppingCart;

import java.util.List;

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
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.order.OrderService;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class CartItemController {
	
	@Autowired private CartItemService cartService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private OrderService orderService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private CustomerService customerService;
	

	@GetMapping("/manage_booking")
	public String getManageBooking(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		return listByPage(loggedCustomer, googleLogin, 1, model, "id", "asc", null);
	}
	
	@GetMapping("/manage_booking/{pageNum}")
	public String listByPage(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, @PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			pagingCartItem(pageNum, sortField, sortDir, keyword, customer, model);
			pagingOrder(pageNum, sortField, sortDir, customer, model);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			pagingCartItem(pageNum, sortField, sortDir, keyword, customer, model);
			pagingOrder(pageNum, sortField, sortDir, customer, model);
		}
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		
		return "cart/manage_booking";
	} 
	
	private Page<Order> pagingOrder(int pageNum, String sortField, String sortDir, Customer customer, Model model) {
		Page<Order> pageOrder = orderService.listByPageOrder(customer, pageNum, sortField, sortDir);
		
		List<Order> listOrders = pageOrder.getContent();
		long startCount1 = (pageNum - 1) * OrderService.ORDER_PER_PAGE + 1;
		long endCount1 = startCount1 + OrderService.ORDER_PER_PAGE - 1;
		if (endCount1 > pageOrder.getTotalElements()) {
			endCount1 = pageOrder.getTotalElements();
		}
		OrderStatus orderStatus = OrderStatus.NEW;
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages1", pageOrder.getTotalPages());
		model.addAttribute("totalItems1", pageOrder.getTotalElements());
		model.addAttribute("startCount1", startCount1);
		model.addAttribute("endCount1", endCount1);
		model.addAttribute("listOrders", listOrders);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("orderStatus", orderStatus);
		model.addAttribute("reverseSort", reverseSort);
		
		return pageOrder;
	}

	private Page<CartItem> pagingCartItem(int pageNum, String sortField, String sortDir, String keyword,
			Customer customer, Model model) {
		Page<CartItem> pageCart = cartService.listByPage(pageNum, sortField, sortDir, keyword, customer);
		
		List<CartItem> listItems = pageCart.getContent();
		for (CartItem cartItem : listItems) {
			List<SearchHistory> findByItem = searchRepo.findByCartItem(cartItem);
			for (SearchHistory history : findByItem) {
				model.addAttribute("history", history);
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
