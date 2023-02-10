package com.easygofly.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.easygofly.entity", "com.easygofly.admin.user"})
public class EasyGoFlyBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyGoFlyBackEndApplication.class, args);
	}

}
