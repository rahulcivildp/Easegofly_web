package com.easygofly.api.web;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSettingsRestController {
	@Autowired private WebSettingService settingService;

	@GetMapping("/settings/about-list")
	public AboutResponse aboutPage(HttpServletResponse response) {
        response.setContentType("application/json");
        AboutResponse responseAbout = new AboutResponse();
        PolicyDetailBag settingBag = settingService.getGeneralSettingBag();

        responseAbout.setAbout(settingBag.getABOUT_US());
        responseAbout.setTerms(settingBag.getTERMS());
        responseAbout.setPrivacy(settingBag.getPRIVACY());
        responseAbout.setAgreement(settingBag.getAGREEMENT());
        responseAbout.setPayment(settingBag.getSECURITY());
        responseAbout.setRefund(settingBag.getREFUND_CANCELLATION());
        responseAbout.setBank(settingBag.getBank());
        responseAbout.setService(settingBag.getSERVICE());
        responseAbout.setFaq(settingBag.getFAQ());

		return responseAbout;
	}
	

	@GetMapping("/settings/contact-us")
	public ContactResponse contactUsPage(HttpServletResponse response) {
        response.setContentType("application/json");
        ContactResponse responseContact = new ContactResponse();
        PolicyDetailBag settingBag = settingService.getGeneralSettingBag();

        responseContact.setService(settingBag.getSERVICE());
        responseContact.setContactUs(settingBag.getCONTACT_US());
        responseContact.setEmailAdmin(settingBag.getADMIN_EMAILS());
        responseContact.setAdminAddress(settingBag.getADMIN_ADDRESS());
        responseContact.setFacebook(settingBag.getFACEBOOK());
        responseContact.setInstagram(settingBag.getINSTAGRAM());
        responseContact.setPhone(settingBag.getWHATSAPP());
        responseContact.setTwitter(settingBag.getTWITTER());

		return responseContact;
	}
	
	// Static POJO List
	
    @SuppressWarnings("unused")
	private static class AboutResponse {
        private String about;
        private String terms;
        private String privacy;
        private String agreement;
        private String payment;
        private String refund;
        private String bank;
        private String service;
        private String faq;
        
		public String getAbout() {
			return about;
		}
		public void setAbout(String about) {
			this.about = about;
		}
		public String getTerms() {
			return terms;
		}
		public void setTerms(String terms) {
			this.terms = terms;
		}
		public String getPrivacy() {
			return privacy;
		}
		public void setPrivacy(String privacy) {
			this.privacy = privacy;
		}
		public String getAgreement() {
			return agreement;
		}
		public void setAgreement(String agreement) {
			this.agreement = agreement;
		}
		public String getPayment() {
			return payment;
		}
		public void setPayment(String payment) {
			this.payment = payment;
		}
		public String getRefund() {
			return refund;
		}
		public void setRefund(String refund) {
			this.refund = refund;
		}
		public String getBank() {
			return bank;
		}
		public void setBank(String bank) {
			this.bank = bank;
		}
		public String getService() {
			return service;
		}
		public void setService(String service) {
			this.service = service;
		}
		public String getFaq() {
			return faq;
		}
		public void setFaq(String faq) {
			this.faq = faq;
		}
    }
    
    @SuppressWarnings("unused")
	private static class ContactResponse {
        private String contactUs;
        private String emailAdmin;
        private String adminAddress;
        private String facebook;
        private String instagram;
        private String phone;
        private String twitter;
        private String service;
		public String getContactUs() {
			return contactUs;
		}
		public void setContactUs(String contactUs) {
			this.contactUs = contactUs;
		}
		public String getEmailAdmin() {
			return emailAdmin;
		}
		public void setEmailAdmin(String emailAdmin) {
			this.emailAdmin = emailAdmin;
		}
		public String getAdminAddress() {
			return adminAddress;
		}
		public void setAdminAddress(String adminAddress) {
			this.adminAddress = adminAddress;
		}
		public String getFacebook() {
			return facebook;
		}
		public void setFacebook(String facebook) {
			this.facebook = facebook;
		}
		public String getInstagram() {
			return instagram;
		}
		public void setInstagram(String instagram) {
			this.instagram = instagram;
		}
		public String getPhone() {
			return phone;
		}
		public void setPhone(String phone) {
			this.phone = phone;
		}
		public String getTwitter() {
			return twitter;
		}
		public void setTwitter(String twitter) {
			this.twitter = twitter;
		}
		public String getService() {
			return service;
		}
		public void setService(String service) {
			this.service = service;
		}
        
        
    }
}
