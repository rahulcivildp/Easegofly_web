package com.easygofly.entity;

import java.util.Objects;

public class CommonSet {
	
	private String key;
	
	private String value;

	public CommonSet() {}

	public CommonSet(String key) {
		this.key = key;
	}
	
	public CommonSet(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonSet other = (CommonSet) obj;
		return Objects.equals(key, other.key);
	}
	
}
