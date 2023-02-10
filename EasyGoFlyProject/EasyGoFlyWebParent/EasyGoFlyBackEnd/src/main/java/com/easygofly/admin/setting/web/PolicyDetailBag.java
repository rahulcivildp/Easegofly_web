package com.easygofly.admin.setting.web;

import java.util.List;

import com.easygofly.entity.WebDetails;
import com.easygofly.entity.WebDetailsBag;

public class PolicyDetailBag extends WebDetailsBag{

	public PolicyDetailBag(List<WebDetails> webDetails) {
		super(webDetails);
	}

	public void updateAdImage(String value) {
		super.update("AD_IMAGE", value);
	}
}
