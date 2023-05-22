package com.easygofly.site.setting;

import java.util.List;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingBag;


public class GeneralSettingBag extends SettingBag {

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
