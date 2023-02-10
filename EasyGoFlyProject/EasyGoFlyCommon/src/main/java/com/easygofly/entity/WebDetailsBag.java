package com.easygofly.entity;

import java.util.List;

public class WebDetailsBag {
	
	private List<WebDetails> details;

	public WebDetailsBag(List<WebDetails> details) {
		this.details = details;
	}
	
	public WebDetails get(String key) {
		int index = details.indexOf(new WebDetails(key));
		if(index >= 0) {
			return details.get(index);
		}
		
		return null;
	}
	
	public String getValue(String key) {
		WebDetails detail = get(key);
		if (detail != null) {
			return detail.getValue();
		}
		
		return null;
	}
	
	public void update(String key, String value) {
		WebDetails detail = get(key);
		if(detail != null && value != null) {
			detail.setValue(value);
		}
	}
	
	public List<WebDetails> list() {
		return details;
	}
}
