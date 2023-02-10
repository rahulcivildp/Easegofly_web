package com.easygofly.site;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;


@XmlAccessorType(value = XmlAccessType.NONE)
@JsonRootName(value = "urlset", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class XmlUrlSet {

	@XmlElements({@XmlElement(name = "url", type = XmlUrl.class, required = true)})
	@JsonProperty(value = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private XmlUrl xmlUrls;

	public XmlUrl getXmlUrls() {
		return xmlUrls;
	}

	public void setXmlUrls(XmlUrl xmlUrls) {
		this.xmlUrls = xmlUrls;
	}

  
}