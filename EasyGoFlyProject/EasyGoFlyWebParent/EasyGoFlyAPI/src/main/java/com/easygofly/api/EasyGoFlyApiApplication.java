package com.easygofly.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@SpringBootApplication
@EnableScheduling
@EntityScan({"com.easygofly.entity", "com.easygofly.api.customer"})
@OpenAPIDefinition(
		info = @Info(
				title = "Easegofly Service API", 
				version = "1.0", 
				description = "API for managing customer accounts in the microservices architecture",
				contact = @Contact(
						name = "Accounts Service Team",
						email = "rahulcivildp@gmail.com"
				),
				license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
				termsOfService = "https://example.com/terms-of-service"
		),
		externalDocs = @ExternalDocumentation(
                description = "Find out more about the Accounts Service",
                url = "https://example.com/accounts-service-docs"
        )
)
public class EasyGoFlyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyGoFlyApiApplication.class, args);
	}

}
