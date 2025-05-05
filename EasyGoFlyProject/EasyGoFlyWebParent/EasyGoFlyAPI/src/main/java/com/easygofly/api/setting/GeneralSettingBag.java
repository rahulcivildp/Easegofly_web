package com.easygofly.api.setting;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;
import com.easygofly.entity.SettingCategory;


public class GeneralSettingBag extends SettingBag {
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
	
	public GeneralSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	public void updateCurrentSymbol(String value) {
		super.update("CURRENCY_SYMBOL", value);
	}
	
	public void updateSiteLogo(String value) {
		super.update("SITE_LOGO", value);
	}
	
	public String getSiteLogo() {
		return super.getValue("SITE_LOGO");
	}
	
	public void updateFavicon(String value) {
		super.update("FAVICON", value);
	}
	
	public String getFavicon() {
		return super.getValue("FAVICON");
	}
}
