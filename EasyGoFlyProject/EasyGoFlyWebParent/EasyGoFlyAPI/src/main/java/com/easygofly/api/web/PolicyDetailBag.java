package com.easygofly.api.web;

import java.util.List;

import com.easygofly.entity.WebDetails;
import com.easygofly.entity.WebDetailsBag;

public class PolicyDetailBag extends WebDetailsBag{

	public PolicyDetailBag(List<WebDetails> webDetails) {
		super(webDetails);
	}
	
	public String getABOUT_US() {
		return super.getValue("ABOUT_US");
	}
	
	public String getAGREEMENT() {
		return super.getValue("AGREEMENT");
	}
	
	public String getBank() {
		return super.getValue("BANK");
	}
	
	public String getFAQ() {
		return super.getValue("FAQ");
	}
	
	public String getPRIVACY() {
		return super.getValue("PRIVACY");
	}
	
	public String getREFUND_CANCELLATION() {
		return super.getValue("REFUND_CANCELLATION");
	}
	
	public String getSECURITY() {
		return super.getValue("SECURITY");
	}
	
	public String getSERVICE() {
		return super.getValue("SERVICE");
	}
	
	public String getTERMS() {
		return super.getValue("TERMS");
	}
	
	public String getADMIN_ADDRESS() {
		return super.getValue("ADMIN_ADDRESS");
	}
	
	public String getADMIN_EMAILS() {
		return super.getValue("ADMIN_EMAILS");
	}
	
	public String getCONTACT_US() {
		return super.getValue("CONTACT_US");
	}
	
	public String getFACEBOOK() {
		return super.getValue("FACEBOOK");
	}
	
	public String getINSTAGRAM() {
		return super.getValue("INSTAGRAM");
	}
	
	public String getTWITTER() {
		return super.getValue("TWITTER");
	}
	
	public String getWHATSAPP() {
		return super.getValue("WHATSAPP");
	}

	public void updateAdImage(String value) {
		super.update("AD_IMAGE", value);
	}
}
