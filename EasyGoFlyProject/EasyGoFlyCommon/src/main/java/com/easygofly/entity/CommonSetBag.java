package com.easygofly.entity;

import java.util.List;

public class CommonSetBag {
	
	private List<CommonSet> listCommonSets;

	public CommonSetBag(List<CommonSet> listCommonSets) {
		this.listCommonSets = listCommonSets;
	}
	
	public CommonSet get(String key) {
		int index = listCommonSets.indexOf(new CommonSet(key));
		if(index >= 0) {
			return listCommonSets.get(index);
		}
		
		return null;
	}
	
	public String getValue(String key) {
		CommonSet setting = get(key);
		if (setting != null) {
			return setting.getValue();
		}
		
		return null;
	}
	
	public void update(String key, String value) {
		CommonSet setting = get(key);
		if(setting != null && value != null) {
			setting.setValue(value);
		}
	}
	
	public List<CommonSet> list() {
		return listCommonSets;
	}
}
