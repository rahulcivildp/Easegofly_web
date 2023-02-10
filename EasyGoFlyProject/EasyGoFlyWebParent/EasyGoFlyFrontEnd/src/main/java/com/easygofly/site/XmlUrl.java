package com.easygofly.site;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;


@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement(name = "url")
public class XmlUrl {
    public enum Priority {
        HIGH("1.0"), MEDIUM("0.5");

        private String value;

        Priority(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @XmlElement
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String loc;

    @XmlElement
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String lastmod;

    @XmlElement
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String changefreq = "daily";

    @XmlElement
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String priority;

    public XmlUrl() {
        setLastmod();
    }

    public XmlUrl(String loc, Priority priority) {
        this.loc = loc;
        this.priority = priority.getValue();
        setLastmod();
    }

    private void setLastmod() {
        this.lastmod = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getLoc() {
        return loc;
    }

    public String getPriority() {
        return priority;
    }

    public String getChangefreq() {
        return changefreq;
    }

    public String getLastmod() {
        return lastmod;
    }
}
