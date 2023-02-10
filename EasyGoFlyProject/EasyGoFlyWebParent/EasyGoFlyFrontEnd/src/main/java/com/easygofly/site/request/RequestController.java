package com.easygofly.site.request;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Request;
import com.easygofly.site.Utility;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.EmailSettingBag;
import com.easygofly.site.setting.SettingService;

@Controller
public class RequestController {

	@Autowired private RequestService requestService;
	@Autowired private CustomerService customerService;
	@Autowired private SettingService settingService;
	
	@PostMapping("/request/save")
	public String saveRequest(Request customerRequest, 
			RedirectAttributes redirectAttributes, 
			HttpServletRequest request,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			@RequestParam(name = "conversation", required = false) String conversation) throws UnsupportedEncodingException, MessagingException {
		
		System.out.println("Customer Request ID: " + customerRequest.getId());
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			saveRequestPart(customerRequest, redirectAttributes, request, conversation, email, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			saveRequestPart(customerRequest, redirectAttributes, request, conversation, email, customer);
		}
		
		return "redirect:/account";
	}

	private void saveRequestPart(Request customerRequest, RedirectAttributes redirectAttributes,
			HttpServletRequest request, String conversation, String email, Customer customer)
			throws UnsupportedEncodingException, MessagingException {
		Date createdTime = new Date();
		System.out.println("Requester ID: " + customer.getId() + " Email: " + customer.getEmail());

		Request savedRequest = requestService.saveRequest(customerRequest, customer, createdTime);
		
		RequestSaveHelper.setConversation(savedRequest, email, conversation, createdTime);
		requestService.saveConversation(customerRequest);
		
		sendRequestAndConversationEmail(request, conversation, email, savedRequest.getSubject(), customerRequest);
		
		redirectAttributes.addFlashAttribute("message", "Your Request is sent to the support team.");
	}
	
	private void sendRequestAndConversationEmail(HttpServletRequest request, String conversation, String emailFrom, String subjectOfEmail, Request customerRequest) throws UnsupportedEncodingException, MessagingException {
		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
		
		String toAddress = "support@easegofly.com";
		String subject = subjectOfEmail;
		String content = "Customer Email: &nbsp;&nbsp;" + emailFrom + "\r\n" + "<br><br>\r\n" + "<p style='margin-bottom: 8px;'>Request Details: </p>"+ conversation ;
		
		MimeMessage message= mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);
		
		mailSender.send(message);
		
		System.out.println("To Address: " + toAddress);
	}
}
