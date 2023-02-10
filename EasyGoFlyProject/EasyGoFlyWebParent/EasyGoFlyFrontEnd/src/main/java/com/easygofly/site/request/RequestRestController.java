package com.easygofly.site.request;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Conversation;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Request;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class RequestRestController {
	
	@Autowired private RequestRepository requestRepo;
	@Autowired private ConversationRepository conversationRepo;
	@Autowired private CustomerService customerService;
	
	@GetMapping("/request/conversation_list")
	public List<Conversation> viewConversation(Request request,RedirectAttributes redirectAttributes) {
		Request requestInSIte = requestRepo.findById(7).get();
		
		return conversationRepo.findConversationByRequest(requestInSIte);
	}
	
	@PostMapping("/request/conversation_save")
	public String sendConversation(@RequestBody Conversation conversation,
			@RequestParam(name = "request_id") Integer request_id, 
			RedirectAttributes redirectAttributes, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			conversation.setRepliedFrom(customer.getEmail());
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			conversation.setRepliedFrom(customer.getEmail()); 
		}
		Request request = requestRepo.findById(request_id).get();
		
		conversation.setCreatedTime(new Date());
		conversation.setRequest(request);
		
		
		Conversation savedConversation = conversationRepo.save(conversation);
		
		redirectAttributes.addFlashAttribute("message", "Message Sent!");
		
		return String.valueOf(savedConversation.getId());
	}
}
