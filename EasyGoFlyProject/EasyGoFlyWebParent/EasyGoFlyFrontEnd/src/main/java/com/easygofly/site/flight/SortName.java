package com.easygofly.site.flight;

public class SortName {
	private String sort;
	private String sortName;

	
	
	public SortName() {}

	public SortName(String sort, String sortName) {
		this.sort = sort;
		this.sortName = sortName;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}
}