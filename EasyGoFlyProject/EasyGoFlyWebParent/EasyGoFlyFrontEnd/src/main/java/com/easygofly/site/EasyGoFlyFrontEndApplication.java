package com.easygofly.site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.easygofly.entity", "com.easygofly.site.customer"})
public class EasyGoFlyFrontEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyGoFlyFrontEndApplication.class, args);
	}

}
