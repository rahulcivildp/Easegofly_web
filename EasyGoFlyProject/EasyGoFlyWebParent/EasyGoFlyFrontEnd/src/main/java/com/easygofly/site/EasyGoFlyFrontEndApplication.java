package com.easygofly.site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan({"com.easygofly.entity", "com.easygofly.site.customer"})
public class EasyGoFlyFrontEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyGoFlyFrontEndApplication.class, args);
	}

}
