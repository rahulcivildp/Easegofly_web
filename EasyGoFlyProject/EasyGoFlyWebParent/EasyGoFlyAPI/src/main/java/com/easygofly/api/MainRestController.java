package com.easygofly.api;


import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.api.customer.CustomerRepository;
import com.easygofly.api.flight.CityRepository;
import com.easygofly.api.flight.SearchHistoryRepository;


@RestController
public class MainRestController {
	@Autowired CityRepository cityRepo;
	@Autowired CustomerRepository customerRepo;
	@Autowired SearchHistoryRepository historyRepo;
	
	@GetMapping("/hello")
    public String hello(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String responseBody = "{"
        		+ "\"code\": 0, "
        		+ "\"msg\": \"Hello, authenticatefghd user!\" "
        		+ "}";
        return responseBody;
    }

}

