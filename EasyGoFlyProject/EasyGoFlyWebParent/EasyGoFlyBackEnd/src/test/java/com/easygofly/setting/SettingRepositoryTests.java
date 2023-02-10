package com.easygofly.setting;

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
import com.easygofly.admin.setting.SettingRepository;
import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingCategory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class SettingRepositoryTests {
	
	@Autowired
	SettingRepository repo;
	
	@Test
	public void testCreateGeneralSettings() {
		Setting siteName = new Setting("SITE_NAME", "air2fly", SettingCategory.GENERAL);
		//Setting savedObject = repo.save(siteName);
		Setting siteLogo = new Setting("SITE_LOGO", "Shopme.png", SettingCategory.GENERAL);
		Setting copyRight = new Setting("COPYRIGHT", "Copyright (C) 2021 Shopme Ltd.", SettingCategory.GENERAL);
		
		repo.saveAll(List.of(siteLogo, copyRight, siteName));
		
		Iterable<Setting> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
		//assertThat(savedObject).isNotNull();
	}
	
	@Test
	public void testCreateCurrencySettings() {
		Setting currencyId = new Setting("CURRENCY_ID", "1", SettingCategory.CURRENCY);
		Setting symbol = new Setting("CURRENCY_SYMBOL", "$", SettingCategory.CURRENCY);
		Setting symbolPosition = new Setting("CURRENCY_SYMBOL_POSITION", "before", SettingCategory.CURRENCY);
		Setting deciamPointType = new Setting("DECIMAL_POINT_TYPE", "POINT", SettingCategory.CURRENCY);
		Setting decimalDigits = new Setting("DECIMAL_DIGITS", "2", SettingCategory.CURRENCY);
		Setting thousandsPointType = new Setting("THOUSANDS_POINT_TYPE", "COMMA", SettingCategory.CURRENCY);
		
		repo.saveAll(List.of(currencyId, symbol, symbolPosition, deciamPointType, decimalDigits, thousandsPointType));
		
		Iterable<Setting> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testCreateMailServerSettings() {
		Setting mailHost = new Setting("MAIL_HOST", "smtp.gmail.com", SettingCategory.MAIL_SERVER);
		Setting mailPort = new Setting("MAIL_PORT", "123", SettingCategory.MAIL_SERVER);
		Setting mailUsername = new Setting("MAIL_USERNAME", "mailUsername", SettingCategory.MAIL_SERVER);
		Setting mailPassword = new Setting("MAIL_PASSWORD", "mailPassword", SettingCategory.MAIL_SERVER);
		Setting smtpAuth = new Setting("SMTP_AUTH", "true", SettingCategory.MAIL_SERVER);
		Setting smtpSecured = new Setting("SMTP_SECURED", "true", SettingCategory.MAIL_SERVER);
		Setting mailFrom = new Setting("MAIL_FROM", "easygofly@gmail.com", SettingCategory.MAIL_SERVER);
		Setting mailSenderName = new Setting("MAIL_SENDER_NAME", "easygofly team", SettingCategory.MAIL_SERVER);
		
		repo.saveAll(List.of(mailHost, mailPort, mailUsername, mailPassword, smtpAuth, smtpSecured, mailFrom, mailSenderName));
		
		Iterable<Setting> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testCreateMailTemplateSettings() {
		Setting customerVerifySubject = new Setting("CUSTOMER_VERIFY_SUBJECT", "Please verfify your registration to continue shopping.", SettingCategory.MAIL_TEMPLATES);
		Setting customerVerifyContent = new Setting("CUSTOMER_VERIFY_CONTENT", "Dear [[name]], Please click on the link below to verify your email.", SettingCategory.MAIL_TEMPLATES);
		
		repo.saveAll(List.of(customerVerifySubject, customerVerifyContent));
		
		Iterable<Setting> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testCreateMailTemplateSettingsOrderConfirmation() {
		Setting OrderConfirmationSubject = new Setting("ORDER_CONFIRMATION_SUBJECT", "Confirmation of you order ID#", SettingCategory.MAIL_TEMPLATES);
		Setting OrderConfirmationContent = new Setting("ORDER_CONFIRMATION_CONTENT", "Dear [[name]], you have successfully confirmed the order. Thank you!", SettingCategory.MAIL_TEMPLATES);
		
		repo.saveAll(List.of(OrderConfirmationSubject, OrderConfirmationContent));
		
		Iterable<Setting> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testCreatePaymentTemplateSettings() {
		Setting paymentAPIbaseURL = new Setting("PAYMENT_API_BASE_URL", "http://", SettingCategory.PAYMENT);
		Setting paymentClientId = new Setting("PAYMENT_CLIENT_ID", "PAYMENT_CLIENT_ID", SettingCategory.PAYMENT);
		Setting paymentClientSecretKey = new Setting("PAYMENT_CLIENT_SECRET_KEY", "PAYMENT_CLIENT_SECRET_KEY", SettingCategory.PAYMENT);
		
		repo.saveAll(List.of(paymentAPIbaseURL, paymentClientId, paymentClientSecretKey));
		
		Iterable<Setting> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testListSettingsByCategory() {
		List<Setting> settings = repo.findByCategory(SettingCategory.GENERAL);
		
		settings.forEach(System.out::println);
	}

}
