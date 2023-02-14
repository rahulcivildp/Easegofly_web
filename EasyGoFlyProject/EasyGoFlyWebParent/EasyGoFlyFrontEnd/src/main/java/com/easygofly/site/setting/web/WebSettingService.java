package com.easygofly.site.setting.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.WebDetails;
import com.easygofly.entity.WebSettingCategory;

@Service
public class WebSettingService  {
	
	@Autowired
	private WebSettingRepository repo;
	
	public List<WebDetails> listAllSettings() {
		return (List<WebDetails>) repo.findAll();
	}
	
	public PolicyDetailBag getGeneralSettingBag() {
		List<WebDetails> webDetails = new ArrayList<>();
		List<WebDetails> detailsPolicy = repo.findByCategory(WebSettingCategory.POLICY);
		List<WebDetails> detailsAdvertisement = repo.findByCategory(WebSettingCategory.ADVERTISEMENT);
		
		webDetails.addAll(detailsPolicy);
		webDetails.addAll(detailsAdvertisement);
		
		return new PolicyDetailBag(webDetails);
		
	}
	
	public List<WebDetails> getGeneralSetting() {
		return repo.findByAllCategories(WebSettingCategory.POLICY, WebSettingCategory.CONTACT, WebSettingCategory.IMAGE, WebSettingCategory.LOWEST_FARE_FINDER);
	}
	
	public void saveAll(Iterable<WebDetails> settings) {
		repo.saveAll(settings);
	}
}
