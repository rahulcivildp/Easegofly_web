package com.easygofly.setting.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.web.WebSettingRepository;
import com.easygofly.entity.WebDetails;
import com.easygofly.entity.WebSettingCategory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class WebSettingRepositoryTest {

	@Autowired WebSettingRepository repo;
	
	@Test
	public void testCreatePolicyDetails() {
		WebDetails faq = new WebDetails("FAQ", "something", WebSettingCategory.POLICY);
		WebDetails aboutUs = new WebDetails("ABOUT_US", "something", WebSettingCategory.POLICY);
		WebDetails terms = new WebDetails("TERMS", "something", WebSettingCategory.POLICY);
		WebDetails privacy = new WebDetails("PRIVACY", "something", WebSettingCategory.POLICY);
		WebDetails agrrement = new WebDetails("AGREEMENT", "something", WebSettingCategory.POLICY);
		WebDetails security = new WebDetails("SECURITY", "something", WebSettingCategory.POLICY);
		WebDetails service = new WebDetails("SERVICE", "something", WebSettingCategory.POLICY);
		WebDetails bank = new WebDetails("BANK", "something", WebSettingCategory.POLICY);
		
		repo.saveAll(List.of(faq, aboutUs, terms, privacy, agrrement, security, service, bank));
		
		Iterable<WebDetails> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testCreateContactDetails() {
		WebDetails whatsapp = new WebDetails("WHATSAPP", "something", WebSettingCategory.CONTACT);
		WebDetails facebook = new WebDetails("FACEBOOK", "something", WebSettingCategory.CONTACT);
		WebDetails twitter = new WebDetails("TWITTER", "something", WebSettingCategory.CONTACT);
		WebDetails instagram = new WebDetails("INSTAGRAM", "something", WebSettingCategory.CONTACT);
		WebDetails youtube = new WebDetails("YOUTUBE", "something", WebSettingCategory.CONTACT);
		WebDetails linkedin = new WebDetails("LINKEDIN", "something", WebSettingCategory.CONTACT);
		WebDetails contactUs = new WebDetails("CONTACT_US", "something", WebSettingCategory.CONTACT);
		WebDetails support = new WebDetails("SUPPORT", "something", WebSettingCategory.CONTACT);
		
		repo.saveAll(List.of(whatsapp, facebook, twitter, instagram, youtube, linkedin, contactUs, support));
		
		Iterable<WebDetails> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testCreateContactDetailsAddress() {
		WebDetails adminAddress = new WebDetails("ADMIN_ADDRESS", "something", WebSettingCategory.CONTACT);
		
		repo.save(adminAddress);
	}
	
	@Test
	public void testCreateImageDetails() {
		WebDetails adImages = new WebDetails("AD_IMAGE", "something", WebSettingCategory.IMAGE);
		WebDetails sliderImages = new WebDetails("SLIDER_IMAGE", "something", WebSettingCategory.IMAGE);
		
		repo.saveAll(List.of(adImages, sliderImages));
		
		Iterable<WebDetails> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testListDetailsByCategory() {
		List<WebDetails> webDetails = repo.findByCategory(WebSettingCategory.POLICY);
		
		webDetails.forEach(System.out::println);
	}
	
	@Test
	public void testSaveDetailsWithImages() {
		String settingKey = "SLIDER_IMAGE";
		WebDetails webImage = repo.findByKey(settingKey);
		
		webImage.addSliderImages("main i image.jpg");
		webImage.addSliderImages("main ise.jpg");
		webImage.addSliderImages("msi image.jpg");
		
		WebDetails savedProduct = repo.save(webImage);
		
		assertThat(savedProduct.getSliderImage().size()).isEqualTo(3);
	}
}
