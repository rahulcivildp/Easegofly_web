package com.easygofly.admin.request;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.user.UserService;
import com.easygofly.entity.Conversation;
import com.easygofly.entity.Request;
import com.easygofly.entity.exception.UserNotFoundException;

@Controller
public class RequestController {

	@Autowired private RequestService requestService;
	@Autowired private RequestRepository requestRepo;
	@Autowired private ConversationRepository conversationRepo;

	@GetMapping("/requests")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "createdTime", "asc", null);
	}
	
	@GetMapping("/requests/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, 
			@Param("sortField") String sortField, @Param("sortDir") String sortDir, 
			@Param("keyword") String keyword) {
		
		Page<Request> pageRequest = requestService.listByPage(pageNum, sortField, sortDir, keyword);
		
		List<Request> listRequests = pageRequest.getContent();
		
		long startCount = (pageNum - 1) * UserService.USER_PER_PAGE + 1;
		long endCount = startCount + UserService.USER_PER_PAGE - 1;
		if (endCount > pageRequest.getTotalElements()) {
			endCount = pageRequest.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageRequest.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageRequest.getTotalElements());
		model.addAttribute("listRequests", listRequests);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		
		return "request/request";
	}
	
	@GetMapping("/requests/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			requestService.deleteRequest(id);
			redirectAttributes.addFlashAttribute("warning", "Request ID: " + id + " is deleted successfully.");
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/requests";
	}
	
	@GetMapping("/request_conversation_{id}")
	public String viewConversation(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		Request request = requestRepo.findById(id).get();
		List<Conversation> listConversations = conversationRepo.findConversationByRequest(request);
		Conversation conversation = new Conversation();
		
		model.addAttribute("conversation", conversation);
		model.addAttribute("listConversations", listConversations);
		model.addAttribute("request", request);
		return "request/conversation";
	}
	
	@PostMapping("/request/conversation/save")
	public String sendConversation(@RequestParam(name = "request_id") Integer request_id, 
			@RequestParam(name = "chatBody") String chatBody,
			Conversation conversation, 
			RedirectAttributes redirectAttributes, Model model) {
		
		Request request = requestRepo.findById(request_id).get();
		
		conversation.setCreatedTime(new Date());
		conversation.setRequest(request);
		conversation.setRepliedFrom("support@easegofly.com");
		conversation.setChatBody(chatBody);
		
		conversationRepo.save(conversation);
		
		redirectAttributes.addFlashAttribute("message", "Message Sent!");
		return "redirect:/request_conversation_" + request_id;
	}
}
