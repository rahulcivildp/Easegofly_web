package com.easygofly.site.setting;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingCategory;


@Service
public class SettingService {
	
	@Autowired
	private SettingRepository settingRepo;
	
	public List<Setting> getGeneralSettingBag() {
		return settingRepo.findByTwoCategories(SettingCategory.GENERAL, SettingCategory.CURRENCY);
	}
	
	public EmailSettingBag getEmailSettings() {
		List<Setting> settings = settingRepo.findByCategory(SettingCategory.MAIL_SERVER);
		settings.addAll(settingRepo.findByCategory(SettingCategory.MAIL_TEMPLATES));
		
		return new EmailSettingBag(settings);
	}
	
	public PaymentSettingBag getPaymentSettings() {
		List<Setting> settings = settingRepo.findByCategory(SettingCategory.PAYMENT);
		return new PaymentSettingBag(settings);
	}
}
