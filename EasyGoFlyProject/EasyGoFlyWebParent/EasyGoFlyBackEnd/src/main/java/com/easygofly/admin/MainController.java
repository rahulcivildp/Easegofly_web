package com.easygofly.admin;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.easygofly.admin.customer.CustomerRepository;
import com.easygofly.admin.order.OrderRepository;
import com.easygofly.admin.user.UserRepository;
import com.easygofly.entity.Order;

@Controller
public class MainController {
	@Autowired private CustomerRepository customerRepo;
	@Autowired private UserRepository userRepo;;
	@Autowired private OrderRepository orderRepo;
	
	@GetMapping("/")
	public String viewHomePage(Model model) {
		Iterable<Order> orderIterable = orderRepo.findAll();
		
		List<Order> latestFiveOrders = StreamSupport.stream(orderIterable.spliterator(), false)
			    .sorted(Comparator.comparing(Order::getId).reversed())
			    .filter(order -> order.getProductDetail() != null)
			    .limit(5)
			    .collect(Collectors.toList());

		model.addAttribute("orders", orderIterable);
		model.addAttribute("latestFiveOrders", latestFiveOrders);
		model.addAttribute("totalCustomers", customerRepo.count());
		model.addAttribute("totalUsers", userRepo.count());
		model.addAttribute("totalOrders", orderRepo.count());
		return "index";
	}
	
	@GetMapping("/login")
	public String viewLoginPage() {
		Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "login";
		}
		return "redirect:/";
	}
	
	@GetMapping("/error")
	public String viewErrorPage() {
		return "error";
	}
}
