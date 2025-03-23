package com.easygofly.api;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.easygofly.api.setting.EmailSettingBag;


public class Utility {

	public static String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}
	
	public static JavaMailSenderImpl prepareMailSender(EmailSettingBag emailSettings) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		Integer port = Integer.parseInt(emailSettings.getPort());
		
		mailSender.setHost(emailSettings.getHost());
		mailSender.setPort(port);
		mailSender.setUsername(emailSettings.getUsername());
		mailSender.setPassword(emailSettings.getPassword());
		
		Properties mailProperties = new Properties();
		mailProperties.setProperty("mail.smtp.auth", emailSettings.getSmtpAuth());
		mailProperties.setProperty("mail.smtp.starttls.enable", emailSettings.getSmtpSecured());
//		mailProperties.setProperty("mail.smtp.ssl.enable", "true");
		
		mailSender.setJavaMailProperties(mailProperties);
		
		return mailSender;
	}
}
