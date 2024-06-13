package com.easygofly.admin.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
import com.easygofly.admin.setting.CurrencyRepository;
import com.easygofly.admin.setting.GeneralSettingBag;
import com.easygofly.admin.setting.SettingService;
import com.easygofly.entity.Currency;
import com.easygofly.entity.Setting;


@Controller
public class SettingController {

	@Autowired private SettingService service; 
	
	@Autowired
	private CurrencyRepository currencyRepo;
	
	@GetMapping("/settings")
	public String listAll(Model model) {
		List<Setting> listAllSettings = service.listAllSettings();
		List<Currency> listAllCurrencies = currencyRepo.findAllByOrderByNameAsc();
		
		model.addAttribute("listAllCurrencies", listAllCurrencies);
		
		for (Setting setting : listAllSettings) {
			model.addAttribute(setting.getKey(), setting.getValue());
		}
		
		return "settings/settings";
	}
	
	@PostMapping("/settings/save_general")
	public String saveGeneralSettings(@RequestParam("fileImage") MultipartFile multipartFile, @RequestParam("favicon") MultipartFile multipartFileFavicon, HttpServletRequest request, RedirectAttributes ra) throws IOException {
		GeneralSettingBag generalSetting = service.getGeneralSettingBag();
		
		saveSiteLogo(multipartFile, generalSetting);
		saveFavicon(multipartFileFavicon, generalSetting);
		saveCurrencySymbol(request, generalSetting);
		updateSettingValueInForm(request, generalSetting.list());
		
		ra.addFlashAttribute("message", "General Settings have been updated.");
		
		return "redirect:/settings";
	}

	@PostMapping("/settings/save_mail_server")
	public String saveMailServerSettings(HttpServletRequest request, RedirectAttributes ra) throws IOException {
		List<Setting> mailServerSetting = service.getMailServerSettings();

		updateSettingValueInForm(request, mailServerSetting);
		
		ra.addFlashAttribute("message", "Mail Server Settings have been updated.");
		
		return "redirect:/settings";
	}
	
	@PostMapping("/settings/save_mail_templates")
	public String saveMailTemplateSettings(HttpServletRequest request, RedirectAttributes ra) throws IOException {
		List<Setting> mailTemplateSetting = service.getMailTemplateSettings();

		updateSettingValueInForm(request, mailTemplateSetting);
		
		ra.addFlashAttribute("message", "Mail Template Settings have been updated.");
		
		return "redirect:/settings";
	}
	
	@PostMapping("/settings/save_payments")
	public String savePaymentSettings(HttpServletRequest request, RedirectAttributes ra) throws IOException {
		List<Setting> paymentSetting = service.getPaymentSettings();

		updateSettingValueInForm(request, paymentSetting);
		
		ra.addFlashAttribute("message", "Payment Settings have been updated.");
		
		return "redirect:/settings";
	}
	
	@PostMapping("/settings/save_api_service")
	public String saveAPIServiceSettings(HttpServletRequest request, RedirectAttributes ra) throws IOException {
		List<Setting> apiServiceSetting = service.getAPIServiceSettings();

		updateSettingValueInForm(request, apiServiceSetting);
		
		ra.addFlashAttribute("message", "API Service Settings have been updated.");
		
		return "redirect:/settings";
	}
	
	private void saveSiteLogo(MultipartFile multipartFile, GeneralSettingBag generalSetting) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			String value = "/site-logo/" + fileName;
			generalSetting.updateSiteLogo(value);
			
			String uploadDir = "../site-logo/";
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}
	}
	
	private void saveFavicon(MultipartFile multipartFile, GeneralSettingBag generalSetting) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			String value = "/favicon/" + fileName;
			generalSetting.updateFavicon(value);
			
			String uploadDir = "../favicon/";
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}
	}
	
	private void saveCurrencySymbol(HttpServletRequest request, GeneralSettingBag generalSetting) throws IOException {
		Integer currencyId = Integer.parseInt(request.getParameter("CURRENCY_ID"));
		Optional<Currency> findByIdResult = currencyRepo.findById(currencyId);
		
		if(findByIdResult.isPresent()) {
			Currency currency = findByIdResult.get();
			generalSetting.updateCurrentSymbol(currency.getSymbol());
		}
	}
	
	private void updateSettingValueInForm(HttpServletRequest request, List<Setting> listSettings) {
		for (Setting setting : listSettings) {
			String value = request.getParameter(setting.getKey());
			if (value != null) {
				setting.setValue(value);
			}
		}
		
		service.saveAll(listSettings);
	}
}
