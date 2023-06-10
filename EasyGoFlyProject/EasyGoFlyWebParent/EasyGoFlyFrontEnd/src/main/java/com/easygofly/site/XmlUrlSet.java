package com.easygofly.site;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName(value = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class XmlUrlSet {

	@JsonProperty(value = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
	private XmlUrl xmlUrl = new XmlUrl();

	public XmlUrl getXmlUrl() {
		return xmlUrl;
	}

	public void setXmlUrl(XmlUrl xmlUrl) {
		this.xmlUrl = xmlUrl;
	}

	

}