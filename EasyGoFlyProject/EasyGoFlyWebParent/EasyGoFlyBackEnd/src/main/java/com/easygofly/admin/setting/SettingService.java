package com.easygofly.admin.setting;

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
	
	public List<Setting> listAllSettings() {
		return (List<Setting>) settingRepo.findAll();
	}
	
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
	
	public void saveAll(Iterable<Setting> settings) {
		settingRepo.saveAll(settings);
	}
	
	public List<Setting> getPaymentSettings() {
		return settingRepo.findByCategory(SettingCategory.PAYMENT);
	}
	
	public List<Setting> getMailServerSettings() {
		return settingRepo.findByCategory(SettingCategory.MAIL_SERVER);
	}
	
	public List<Setting> getMailTemplateSettings() {
		return settingRepo.findByCategory(SettingCategory.MAIL_TEMPLATES);
	}
}
