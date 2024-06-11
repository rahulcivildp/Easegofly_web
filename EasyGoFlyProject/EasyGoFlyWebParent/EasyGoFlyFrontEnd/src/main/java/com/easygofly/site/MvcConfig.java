package com.easygofly.site;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		exposedDirectory("../category-photos", registry);
		exposedDirectory("../site-logo", registry);
		exposedDirectory("../favicon", registry);
		exposedDirectory("../brand-logos", registry);
		exposedDirectory("../product-photos", registry);
		exposedDirectory("../customer-photos", registry);
		exposedDirectory("../pdf-images", registry);
		exposedDirectory("../xml-data", registry);
	}

	private void exposedDirectory(String pathPattern, ResourceHandlerRegistry registry) {
		Path photoDir = Paths.get(pathPattern);
		String absolutePhotoPath = photoDir.toFile().getAbsolutePath();
		
		String logicalPath = pathPattern.replace("../", "") + "/**";
		
		registry.addResourceHandler(logicalPath).addResourceLocations("file://" + absolutePhotoPath + "/"); 
	} 
	 
} 
   