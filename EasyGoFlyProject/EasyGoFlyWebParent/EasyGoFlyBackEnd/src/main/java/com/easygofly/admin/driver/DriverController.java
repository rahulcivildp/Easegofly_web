package com.easygofly.admin.driver;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.user.UserService;
import com.easygofly.entity.Cab;
import com.easygofly.entity.Driver;
import com.easygofly.entity.exception.UserNotFoundException;

@Controller
public class DriverController {
	
	@Autowired private DriverService driverService;

	@GetMapping("/drivers")
	public String drivers(Model model) {
		return listByPage(1, model, "id", "asc", null);
	}
	
	@GetMapping("/drivers/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		//System.out.println("Sort Field: " + sortField);
		//System.out.println("Sort Order: " + sortDir);
		Page<Driver> pageDriver = driverService.listByPage(pageNum, sortField, sortDir, keyword);
		
		List<Driver> listDrivers = pageDriver.getContent();
		
		long startCount = (pageNum - 1) * UserService.USER_PER_PAGE + 1;
		long endCount = startCount + UserService.USER_PER_PAGE - 1;
		if (endCount > pageDriver.getTotalElements()) {
			endCount = pageDriver.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageDriver.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageDriver.getTotalElements());
		model.addAttribute("listDrivers", listDrivers);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		
		return "drivers/drivers";
	}
	
	@GetMapping("/drivers/new")
	public String driversNew(Model model) {
		Driver newDriver = new Driver();

		model.addAttribute("driver", newDriver);
		return "drivers/driver-reg";
	}
	
	@PostMapping("/drivers/save_driver")
	public String driversSave(@RequestParam Map<String, String> formData, @RequestParam("image") MultipartFile multipartFileDriver, @RequestParam("cabImg") MultipartFile multipartFileCab, RedirectAttributes redirectAttributes) throws IOException {
		
		double latitude = formData.get("latitude") != null && !formData.get("latitude").isEmpty() ? Double.parseDouble(formData.get("latitude")) : 0.0;
		double longitude = formData.get("longitude") != null && !formData.get("longitude").isEmpty() ? Double.parseDouble(formData.get("longitude")) : 0.0;

		
		 Cab cab = new Cab(formData.get("cabName"), formData.get("type"), Integer.parseInt(formData.get("seating")), Double.parseDouble(formData.get("bookingFare")), formData.get("fuelType"), formData.get("color"), Integer.parseInt(formData.get("maxSpeed")), formData.get("airConditioning"), formData.get("wifi"), formData.get("license"), formData.get("features"));
		 
        Driver driver = new Driver(formData.get("driverName"), Double.parseDouble(formData.get("rating")), Integer.parseInt(formData.get("experience")), formData.get("location"), formData.get("contact"), formData.get("address"), Integer.parseInt(formData.get("coveringDistance")), latitude, longitude, cab);


		Driver savedDriver = driverService.multipartMethodDriver(multipartFileDriver, driver);
		
		Cab savedCab = savedDriver.getCab();

		driverService.multipartMethodCab(multipartFileCab, savedCab);
		
		redirectAttributes.addFlashAttribute("message", "The Driver has been saved sucessfully.");
        
		return "redirect:/drivers";
	}
	
	@PostMapping("/drivers/update_driver")
	public String driversUpdate(@RequestParam Map<String, String> formData, @RequestParam("image") MultipartFile multipartFileDriver, @RequestParam("cabImg") MultipartFile multipartFileCab, RedirectAttributes redirectAttributes) throws IOException {
		
		Integer driverId = Integer.parseInt(formData.get("driverId"));
		Integer cabId = Integer.parseInt(formData.get("cabId"));
		
		driverService.updateDriver(driverId, cabId, formData, multipartFileDriver, multipartFileCab);
		
		redirectAttributes.addFlashAttribute("message", "The Driver has been updated sucessfully.");
        
		return "redirect:/drivers";
	}
	

	@GetMapping("/drivers/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		Driver driver = driverService.findById(id);
		
		model.addAttribute("driver", driver);
		
		return "drivers/driver-update";
	}
	
	@GetMapping("/drivers/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			driverService.deleteDriver(id);
			redirectAttributes.addFlashAttribute("warning", "Driver ID: " + id + " is deleted successfully.");
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/drivers";
	}
	
}
