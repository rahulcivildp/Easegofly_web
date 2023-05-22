package com.easygofly.site.setting;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingCategory;

@Service
public class SettingService {
	
	@Autowired
	private SettingRepository settingRepo;
	
	public GeneralSettingBag getGeneralSettingBag() {
		List<Setting> settings = new ArrayList<>();
		List<Setting> settingsGenaral = settingRepo.findByCategory(SettingCategory.GENERAL);
		List<Setting> settingsCurrency = settingRepo.findByCategory(SettingCategory.CURRENCY);
		
		settings.addAll(settingsGenaral);
		settings.addAll(settingsCurrency);
		
		return new GeneralSettingBag(settings);
		
	}
	
	public List<Setting> getGeneralSetting() {
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
