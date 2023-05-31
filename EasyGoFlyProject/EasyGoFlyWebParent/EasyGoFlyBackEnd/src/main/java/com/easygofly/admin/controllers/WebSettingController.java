package com.easygofly.admin.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.FileUploadUtil;
import com.easygofly.admin.setting.city.CityRepository;
import com.easygofly.admin.setting.web.CouponService;
import com.easygofly.admin.setting.web.PolicyDetailBag;
import com.easygofly.admin.setting.web.WebSettingService;
import com.easygofly.entity.City;
import com.easygofly.entity.Coupon;
import com.easygofly.entity.WebDetails;

@Controller
public class WebSettingController {
	
	@Autowired private WebSettingService service; 
	@Autowired private CityRepository cityRepo;
	@Autowired private CouponService couponService ;
	
	@GetMapping("/web_settings")
	public String listAll(Model model) {
		List<WebDetails> listAllSettings = service.listAllSettings();
		List<City> cities = cityRepo.findAllByOrderByNameAsc();
		List<Coupon> coupons = couponService.listAllCoupons();
		
		for (WebDetails details : listAllSettings) {
			model.addAttribute(details.getKey(), details.getValue());
		}
		model.addAttribute("cities", cities);
		model.addAttribute("coupons", coupons);
		
		return "webDetails/web_settings";
	}
	
	@PostMapping("/web_settings/save_general")
	public String saveGeneralSettings(@RequestParam("fileImage") MultipartFile multipartFile, HttpServletRequest request, RedirectAttributes ra) throws IOException {
		PolicyDetailBag generalSetting = service.getGeneralSettingBag();
		
		saveAdImage(multipartFile, generalSetting);
		updateSettingValueInForm(request, generalSetting.list());
		
		ra.addFlashAttribute("message", "General settings have been updated.");
		
		return "redirect:/web_settings";
	}
	
	@PostMapping("/web_settings/save_contact")
	public String saveContactSettings(HttpServletRequest request, RedirectAttributes ra) throws IOException {
		PolicyDetailBag generalSetting = service.getGeneralSettingBag();
		
		updateSettingValueInForm(request, generalSetting.list());
		
		ra.addFlashAttribute("message", "Contact settings have been updated.");
		
		return "redirect:/web_settings";
	}
	
	@PostMapping("/web_settings/save_lowest_price")
	public String saveLowestPriceSettings(HttpServletRequest request, RedirectAttributes ra) throws IOException {
		PolicyDetailBag generalSetting = service.getGeneralSettingBag();
		
		updateSettingValueInForm(request, generalSetting.list());
		
		ra.addFlashAttribute("message", "Contact settings have been updated.");
		
		return "redirect:/web_settings";
	}
	
	@PostMapping("/web_settings/save_coupon")
	public String saveCouponSettings(@RequestParam(name = "couponAmount") Integer couponAmount) throws IOException {
		couponService.createCoupon(couponAmount);
		
		return "redirect:/web_settings";
	}
	
	private void saveAdImage(MultipartFile multipartFile, PolicyDetailBag generalSetting) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			String value = "/ad-image/" + fileName;
			generalSetting.updateAdImage(value);
			
			String uploadDir = "../ad-image/";
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}
	}
	
	private void updateSettingValueInForm(HttpServletRequest request, List<WebDetails> listSettings) {
		for (WebDetails setting : listSettings) {
			String value = request.getParameter(setting.getKey());
			if (value != null) {
				setting.setValue(value);
			}
		}
		
		service.saveAll(listSettings);
	}
}
