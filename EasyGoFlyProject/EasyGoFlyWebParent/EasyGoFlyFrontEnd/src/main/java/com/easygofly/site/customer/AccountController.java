package com.easygofly.site.customer;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Brand;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Conversation;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.Request;
import com.easygofly.entity.SearchHistory;
import com.easygofly.site.FileUploadUtil;
import com.easygofly.site.flight.order.OrderService;
import com.easygofly.site.request.ConversationRepository;
import com.easygofly.site.request.RequestRepository;
import com.easygofly.site.request.RequestService;
import com.easygofly.site.flight.BrandRepositoy;
import com.easygofly.site.flight.SearchHistoryRepository;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.shoppingCart.CartItemService;


@Controller
public class AccountController {
	
	@Autowired private CustomerService customerService;
	@Autowired private CartItemService cartService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private OrderService orderService;
	@Autowired private RequestService requestService;
	@Autowired private RequestRepository requestRepo;
	@Autowired private ConversationRepository conversationRepo;
	@Autowired private BrandRepositoy brandRepo;
	
	@GetMapping("/account")
	public String viewDetailsCustomer(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User oauthCustomer, Model model) {
		Customer customer; /*= customerService.getByPhone(email);*/
		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
			model.addAttribute("customer", customer);
		} else if (oauthCustomer != null) {
			customer = customerService.getByEmail(oauthCustomer.getEmail());
			model.addAttribute("customer", customer);
		} 
		
		List<Country> listCountries = customerService.listAllCountries();
		Request request = new Request();
		
		Iterable<Brand> brands = brandRepo.findAll();
		
		model.addAttribute("request", request);
		model.addAttribute("listCountries", listCountries);
		model.addAttribute("pageTitle", "Account Page");
		model.addAttribute("newOrder", OrderStatus.NEW);
		model.addAttribute("orderCancelled", OrderStatus.CANCELLED);
		model.addAttribute("orderSuccess", OrderStatus.SUCCESSFULL);
		model.addAttribute("orderFailed", OrderStatus.FAILED);
		model.addAttribute("orderPending", OrderStatus.PENDING);
		model.addAttribute("brands", brands);
		
		listByPage(loggedCustomer, 1, model, "id", "asc", null);
		return "user_credential/account/account";
	}
	 
	@PostMapping("/account/update")
	public String saveCustomer(@ModelAttribute Customer customer, 
			RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			customer.setPhotos(fileName);
			Customer savedCustomer = customerService.updateAccount(customer);
			String uploadDir = "../customer-photos/" + savedCustomer.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if(customer.getPhotos().isEmpty()) {
				customer.setPhotos(null); 
			}
			customerService.updateAccount(customer);
		}
		
		if (loggedCustomer != null) {
			loggedCustomer.setFirstName(customer.getFirstName());
			loggedCustomer.setLastName(customer.getLastName());
			loggedCustomer.setPhoto(customer.getPhotos());
		} 
		
		redirectAttributes.addFlashAttribute("message", "Your account is updated.");
		return "redirect:/account";
	}
	
	@PostMapping("/update_name")
	public String updateNameCustomer(RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@RequestParam(name = "firstName") String firstName, @RequestParam(name = "lastName") String lastName) throws IOException {
			Customer customer;
			
		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
			Customer updatedCustomer = customerService.updateCustomeName(customer, firstName, lastName);
			
			loggedCustomer.setFirstName(updatedCustomer.getFirstName());
			loggedCustomer.setLastName(updatedCustomer.getLastName());
		} 
		
		redirectAttributes.addFlashAttribute("message", "Your account is updated.");
		return "redirect:/";
	}
	
	@GetMapping("/account/{pageNum}")
	public String listByPage(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer,
			@PathVariable(name = "pageNum") int pageNum, Model model, 
			@Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		Customer customer; 
		if (loggedCustomer != null) {
			customer = customerService.getByPhone(loggedCustomer.getUsername());
			pagingCartItem(pageNum, sortField, sortDir, keyword, customer, model);
			pagingOrder(pageNum, sortField, sortDir, customer, model);
			pagingRequest(pageNum, sortField, sortDir, customer, model);
			model.addAttribute("customer", customer);
		}
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		
		return "user_credential/account/account";
	}

	private Page<Request> pagingRequest(int pageNum, String sortField, String sortDir, Customer customer, Model model) {
		Page<Request> pageRequest = requestService.listByPage(pageNum, sortField, sortDir, customer);
		
		List<Request> listRequests = pageRequest.getContent();
		long startCount2 = (pageNum - 1) * RequestService.REQUEST_PER_PAGE + 1;
		long endCount2 = startCount2 + RequestService.REQUEST_PER_PAGE - 1;
		if (endCount2 > pageRequest.getTotalElements()) {
			endCount2 = pageRequest.getTotalElements();
		}
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages2", pageRequest.getTotalPages());
		model.addAttribute("totalItems2", pageRequest.getTotalElements());
		model.addAttribute("startCount2", startCount2);
		model.addAttribute("endCount2", endCount2);
		model.addAttribute("listRequests", listRequests);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		
		return pageRequest;
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
	
	@GetMapping("/request_conversation_{id}")
	public String viewConversation(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		Request request = requestRepo.findById(id).get();
		List<Conversation> listConversations = conversationRepo.findConversationByRequest(request);
		Conversation conversation = new Conversation();
		
		model.addAttribute("conversation", conversation);
		model.addAttribute("listConversations", listConversations);
		model.addAttribute("request", request);
		return "user_credential/account/request/conversation_modal";
	}
	
	@PostMapping("/request/conversation/save")
	public String sendConversation(@RequestParam(name = "request_id") Integer request_id, 
			@RequestParam(name = "chatBody") String chatBody,
			Conversation conversation, 
			RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			conversation.setRepliedFrom(customer.getEmail());
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByPhone(email);
			conversation.setRepliedFrom(customer.getEmail());
		}
		
		Request request = requestRepo.findById(request_id).get();
		
		conversation.setCreatedTime(new Date());
		conversation.setRequest(request);
		conversation.setChatBody(chatBody);
		
		conversationRepo.save(conversation);
		
		redirectAttributes.addFlashAttribute("message", "Message Sent!");
		return "redirect:/request_conversation_" + request_id;
	}
}
