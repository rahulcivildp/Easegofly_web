package com.easygofly.admin.customer;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.FileUploadUtil;
import com.easygofly.admin.user.UserService;
import com.easygofly.entity.Country;
import com.easygofly.entity.Customer;
import com.easygofly.entity.exception.UserNotFoundException;

@Controller
public class CustomerController {

	@Autowired
	private CustomerService service;
	
	@GetMapping("/customers")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "firstName", "asc", null);
		
	}
	
	@GetMapping("/customers/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		//System.out.println("Sort Field: " + sortField);
		//System.out.println("Sort Order: " + sortDir);
		Page<Customer> pageUser = service.listByPage(pageNum, sortField, sortDir, keyword);
		
		List<Customer> listUsers = pageUser.getContent();
		
		long startCount = (pageNum - 1) * UserService.USER_PER_PAGE + 1;
		long endCount = startCount + UserService.USER_PER_PAGE - 1;
		if (endCount > pageUser.getTotalElements()) {
			endCount = pageUser.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageUser.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageUser.getTotalElements());
		model.addAttribute("listUsers", listUsers);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		
		return "customers/customers";
	}
	
	@GetMapping("/customers/new")
	public String newCustomer(Model model) {
		Customer customer = new Customer();
		List<Country> listCountries = service.listAllCountries();
		
		model.addAttribute("listCountries", listCountries);
		model.addAttribute("customer", customer);
		model.addAttribute("pageTitle", "Create Account");
		
		return "customers/customer_form";
	}
	
	@PostMapping("/customers/save")
	public String saveCustomer(Customer customer, RedirectAttributes redirectAttributes, @RequestParam("image") MultipartFile multipartFile) throws IOException {
		
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			customer.setPhotos(fileName);
			Customer savedCustomer = service.saveCustomer(customer);
			String uploadDir = "../customer-photos/" + savedCustomer.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if(customer.getPhotos().isEmpty()) {
				customer.setPhotos(null);
			}
			service.saveCustomer(customer);
		}
		
		redirectAttributes.addFlashAttribute("message", "The user has been saved sucessfully.");
		return getRedirectURLtoAffectedUser(customer);
	}

	private String getRedirectURLtoAffectedUser(Customer customer) {
		String firstPartOfEmail = customer.getEmail().split("@")[0];
		return "redirect:/customers/page/1?sortField=id&sortDir=asc&keyword=" + firstPartOfEmail;
	}
	
	@GetMapping("/customers/edit/{id}")
	public String editCustomer(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Customer customer = service.updateCustomer(id);
			
			model.addAttribute("customer", customer);
			model.addAttribute("pageTitle", "Edit User: ID - " + id);
			
			return "customers/customer_form";
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("alert", e.getMessage());
			return "redirect:/customers";
		}
	}
	
	@GetMapping("/customers/delete/{id}")
	public String deleteCustomer(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			service.deleteCustomer(id);
			redirectAttributes.addFlashAttribute("warning", "User ID: " + id + " is deleted successfully.");
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/customers";
	}
	
	@GetMapping("/customers/{id}/enabled/{status}")
	public String updateUnabledStatus(@PathVariable(name = "id") Integer id, @PathVariable(name = "status") boolean enabled, Model model, RedirectAttributes redirectAttributes) {
		service.updateCustomerEnabledStatus(id, enabled);
		String status = enabled? "ENABLED" : "DISABLED";
		redirectAttributes.addFlashAttribute("message", "User ID: " + id + " is successfully " + status + ".");
		return "redirect:/customers";
	}
}
