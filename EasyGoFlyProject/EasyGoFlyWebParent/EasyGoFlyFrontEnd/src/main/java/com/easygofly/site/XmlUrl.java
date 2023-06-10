package com.easygofly.site;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "url")
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

 
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String loc;

   
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String lastmod;

   
	@JsonProperty(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private String changefreq = "daily";

   
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
