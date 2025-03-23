package com.easygofly.site.customer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.site.FileUploadUtil;
//import com.easygofly.site.Utility;
import com.easygofly.site.security.LoginSuccessHandler;
//import com.easygofly.site.setting.EmailSettingBag;
//import com.easygofly.site.setting.SettingService;

@Controller
public class CustomerController {
	
	@Autowired private CustomerService customerService;
//	@Autowired private SettingService settingService;
	@Autowired private CustomerRepository customerRepo;
	
	@GetMapping("/registration")
	public String newCustomer(Model model, Principal principal, HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		request.getSession().setAttribute(LoginSuccessHandler.REDIRECT_URL_SESSION_ATTRIBUTE_NAME, referer);
		Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			Customer customer = new Customer();
			List<Country> listCountries = customerService.listAllCountries();
			
			model.addAttribute("listCountries", listCountries);
			model.addAttribute("customer", customer);
			model.addAttribute("pageTitle", "Create Account");
			
			return "user_credential/registration";
		}

		return "redirect:/logged";
	}
	
	@PostMapping("/create_customer_account")
	public String saveCustomer(Customer customer, RedirectAttributes redirectAttributes, @RequestParam("image") MultipartFile multipartFile, 
			 @RequestParam("doc") MultipartFile multipartDoc, Model model, HttpServletRequest request) throws IOException, MessagingException, UnsupportedEncodingException {
		Customer existingCustomer = customerRepo.findCustomerByEmail(customer.getEmail());
		
		if (existingCustomer == null) {
			if (!multipartFile.isEmpty()) {
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				customer.setPhotos(fileName);
				Customer savedCustomers = customerService.saveCustomer(customer);
				String uploadDir = "../customer-photos/" + savedCustomers.getId();
				
				FileUploadUtil.cleanDir(uploadDir);
				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
				multipartSave(multipartDoc, savedCustomers);
			} else {
				if(customer.getPhotos().isEmpty()) {
					customer.setPhotos(null);
				}
				customerService.saveCustomer(customer);
				multipartSave(multipartDoc, customer);
			}
			
			model.addAttribute("pageTitle", "Registration Successfull");
			
			redirectAttributes.addFlashAttribute("message", "The user has been saved sucessfully.");
			return "user_credential/registrationSuccess";
			
		} else {
			redirectAttributes.addFlashAttribute("warning", "Duplicate email found...");
			return "redirect:/registration";
		}
		
	}
	
	private void multipartSave(MultipartFile multipartFile, Customer customer) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			customer.setAadhaarDoc(fileName);
			Customer savedCustomers = customerService.saveCustomer(customer);
			String uploadDir = "../customer-aadhaar/" + savedCustomers.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if(customer.getAadhaarDoc().isEmpty()) {
				customer.setAadhaarDoc(null);
			}
		}
	}
//	
//	@PostMapping("/create_customer_account")
//	public String saveCustomer(Customer customer, RedirectAttributes redirectAttributes, @RequestParam("image") MultipartFile multipartFile, Model model, HttpServletRequest request) throws IOException, MessagingException, UnsupportedEncodingException {
//		Customer existingCustomer = customerRepo.findCustomerByEmail(customer.getEmail());
//		
//		if (existingCustomer == null) {
//			if (!multipartFile.isEmpty()) {
//				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//				customer.setPhotos(fileName);
//				Customer savedCustomers = customerService.saveCustomer(customer);
//				String uploadDir = "../customer-photos/" + savedCustomers.getId();
//				
//				sendVerificationEmail(request, customer);
//				
//				FileUploadUtil.cleanDir(uploadDir);
//				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
//			} else {
//				if(customer.getPhotos().isEmpty()) {
//					customer.setPhotos(null);
//				}
//				
//				customerService.saveCustomer(customer);
//				sendVerificationEmail(request, customer);
//			}
//			
//			model.addAttribute("pageTitle", "Registration Successfull");
//			
//			redirectAttributes.addFlashAttribute("message", "The user has been saved sucessfully.");
//			return "user_credential/registrationSuccess";
//			
//		} else {
//			redirectAttributes.addFlashAttribute("warning", "Duplicate email found...");
//			return "redirect:/registration";
//		}
//		
//	}
	
//	private void sendVerificationEmail(HttpServletRequest request, Customer customer) throws UnsupportedEncodingException, MessagingException {
//		EmailSettingBag emailSettings = settingService.getEmailSettings();
//		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
//		
//		String toAddress = customer.getEmail();
//		String subject = emailSettings.getCunstomerVerifySubject();
//		String content = emailSettings.getCunstomerVerifyContent();
//		
//		MimeMessage message= mailSender.createMimeMessage();
//		MimeMessageHelper helper = new MimeMessageHelper(message);
//		
//		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
//		helper.setTo(toAddress);
//		helper.setSubject(subject);
//		
//		content = content.replace("[[name]]", customer.getFullName());
//		
//		String verifyURL = Utility.getSiteURL(request) + "/verify?code=" + customer.getVerificationCode();
//		
//		content = content.replace("[[URL]]", verifyURL);
//		
//		helper.setText(content, true);
//		
//		mailSender.send(message);
//		
//		System.out.println("To Address: " + toAddress);
//		System.out.println("Verify URL: " + verifyURL);
//	}
	
	@GetMapping("/verify")
	public String verifyAccount(@Param("code") String code, Model model) {
		boolean verified = customerService.verify(code);
		
		return "user_credential/" + (verified ? "verify_success" : "verify_failure");
		
	}
	
	@GetMapping("/verify/otp")
	public String verifyAccountByOTP(@Param("code") String code, @Param("phone")String phone, Model model) {
		boolean verified = customerService.verifyByOTP(code, phone);
		
		return "user_credential/" + (verified ? "verify_success" : "verify_failure");
		
	}
	 
}
