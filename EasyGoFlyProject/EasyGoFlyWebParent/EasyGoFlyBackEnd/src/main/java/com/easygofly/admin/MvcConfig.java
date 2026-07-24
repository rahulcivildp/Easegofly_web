package com.easygofly.admin;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		exposeAbsoluteDir("../product-photos", "/product-photos/**", registry);
		exposeAbsoluteDir("../category-photos", "/category-photos/**", registry);
		exposeAbsoluteDir("../brand-logos", "/brand-logos/**", registry);
		exposeAbsoluteDir("../pdf-images", "/pdf-images/**", registry);
		exposeAbsoluteDir("../site-logo", "/site-logo/**", registry);
		exposeAbsoluteDir("../favicon", "/favicon/**", registry);
		exposeAbsoluteDir("user-photos", "/user-photos/**", registry);
		exposeAbsoluteDir("../customer-photos", "/customer-photos/**", registry);
		exposeAbsoluteDir("../driver-photos", "/driver-photos/**", registry);
		exposeAbsoluteDir("../cab-photos", "/cab-photos/**", registry);
		exposeAbsoluteDir("../xml-data", "/xml-data/**", registry);
	}

	private void exposeAbsoluteDir(String fsPath, String urlPattern, ResourceHandlerRegistry registry) {
		Path path = Paths.get(fsPath).toAbsolutePath().normalize();

		System.out.println("Serving " + urlPattern + " from " + path);

		registry.addResourceHandler(urlPattern).addResourceLocations(path.toUri().toString());
	}
 
}
