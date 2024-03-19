package com.easygofly.site;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SitemapController {

	private String DOMAIN = "https://www.easegofly.com/";
	
	  @GetMapping(value = "/sitemap.xml", produces = {"application/xml", "text/xml"})
	    public ResponseEntity<String> generateSitemap() {
	        StringBuilder sitemap = new StringBuilder();
	        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
	               .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n")
	               .append(urlEntry(DOMAIN, "daily", "1.0"))
	               .append(urlEntry(DOMAIN + "hotel", "daily", "1.0"))
	               .append(urlEntry(DOMAIN + "login", "weekly", "0.8"))
	               .append(urlEntryOnlyDuration(DOMAIN + "about"))
	               .append(urlEntry(DOMAIN + "jaipur_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "rishikesh_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "shimla_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "kolkata_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "bangalore_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "darjeeling_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "kerala_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "mumbai_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "visakhaptnam_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "goa_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "haridwar_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "kathmandu_view", "monthly", "0.5"))
	               .append(urlEntry(DOMAIN + "jammu_view", "monthly", "0.5"))
	               // Add more URLs as needed
	               .append("</urlset>");

	        return ResponseEntity.ok(sitemap.toString());
	    }

	    private String urlEntry(String url, String duration, String priority) {
	    	Date date = new Date();
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        String dateTime = dateFormat.format(date);
	        
	        return String.format("<url>\n<loc>%1$s</loc>\n<lastmod>%2$s</lastmod>\n<changefreq>%3$s</changefreq>\n<priority>%4$s</priority>\n</url>\n", url, dateTime, duration, priority);
	    }

	    private String urlEntryOnlyDuration(String url) {
	    	Date date = new Date();
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        String dateTime = dateFormat.format(date);
	        
	        return String.format("<url>\n<loc>%1$s</loc>\n<lastmod>%2$s</lastmod>\n</url>\n", url, dateTime);
	    }
	    
}
