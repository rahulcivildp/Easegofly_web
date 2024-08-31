package com.easygofly.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan({"com.easygofly.entity", "com.easygofly.api.customer"})
public class EasyGoFlyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyGoFlyApiApplication.class, args);
	}

}
