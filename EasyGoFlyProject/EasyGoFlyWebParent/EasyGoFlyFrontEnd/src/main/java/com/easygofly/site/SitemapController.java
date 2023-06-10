package com.easygofly.site;

import java.util.List;

import org.springframework.http.ResponseEntity;

//import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SitemapController {
    private List<String> URLS = List.of("/", "/about#no-8", "/about#no-3", "/about#no-2");
    private String DOMAIN = "https://www.easegofly.com";

    @GetMapping(value = "/sitemap.xml", produces = {"application/xml", "text/xml"})
    @ResponseBody
    public ResponseEntity<XmlUrlSet> main() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();
        /*for (String eachLink : URLS) {
            create(xmlUrlSet, eachLink, XmlUrl.Priority.HIGH);
        }*/
        create(xmlUrlSet, "/", XmlUrl.Priority.HIGH);
       // create(xmlUrlSet, "/link-1", XmlUrl.Priority.HIGH);
        //create(xmlUrlSet, "/link-2", XmlUrl.Priority.MEDIUM);
		
        return ResponseEntity.ok(xmlUrlSet); 
    }

    private void create(XmlUrlSet xmlUrlSet, String link, XmlUrl.Priority priority) {
       xmlUrlSet.setXmlUrl(new XmlUrl(DOMAIN + link, priority));
    }
    
    
}
