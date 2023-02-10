package com.easygofly.site.customer;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Customer;
import com.easygofly.entity.exception.UserNotFoundException;
import com.easygofly.site.Utility;
import com.easygofly.site.setting.EmailSettingBag;
import com.easygofly.site.setting.SettingService;


@Controller
public class ForgotPasswordController {
	@Autowired private CustomerService customerService;
	@Autowired private SettingService settingService;
	@Autowired private CustomerRepository customerRepo;
	
	@GetMapping("/forgot_password")
	public String forgotPassword() {
		
		return "user_credential/forgot_password";
	}
	
	@PostMapping("/forgotPassSendEmail")
	public String forgotPassSendEmail(HttpServletRequest request, @RequestParam(name = "emailId") String emailId, Model model) throws UnsupportedEncodingException, MessagingException {
		
		try {
			System.out.println("EMAIL ID: " + emailId);
			String token= customerService.updateResetPasswordToekn(emailId);
			sendForgotPasswordEmail(request, emailId, token);
			return "user_credential/passLinkSend";
		} catch (UserNotFoundException e) {
			model.addAttribute("error", e.getMessage());
			return "user_credential/forgot_password";
		}
		
	}
	
	private void sendForgotPasswordEmail(HttpServletRequest request, String emailId, String token) throws UnsupportedEncodingException, MessagingException {
		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
		
		String verifyURL = Utility.getSiteURL(request) + "/change-pass?code=" + token;
		
		String toAddress = emailId;
		String subject = "Forgot Password | Easegofly Team";
		String content = "Dear sender,<br><br><br>Please click on the below link to reset your password.<br><br><a style='font-size: 30px; color: red;' href='" + 
						verifyURL + "'>Reset Password</a><br><br><br>Regards,<br>Easegofly Team";

		MimeMessage message= mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);
		
		mailSender.send(message);
		
		System.out.println("To Address: " + toAddress);
		System.out.println("Verify URL: " + verifyURL);
	}
	
	@GetMapping("/change-pass")
	public String changePass(@Param("code") String code, Model model, RedirectAttributes redirectAttributes) {
		boolean verified = customerService.forgotPass(code);
		if (verified) {
			Customer existingCusomer = customerService.findByToken(code);
			
			model.addAttribute("existingCusomer", existingCusomer);
			return "user_credential/change_pass";
		} else {
			redirectAttributes.addAttribute("failure", "Wrong verification code or email id!");
			return "redirect:/login";
		}
	}
	
	@PostMapping("/password-save")
	public String postMethodName(@RequestParam("password") String password, @RequestParam("cust_id") Integer id, RedirectAttributes redirectAttributes) {
		Customer customer = customerRepo.findById(id).get();
		customerService.passwordSave(customer, password);
		redirectAttributes.addAttribute("success", "Your password has been updated. Please login now.");
		return "redirect:/login";
	}
}
