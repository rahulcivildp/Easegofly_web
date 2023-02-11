package com.easygofly.site.middle_body;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MiddleBodyController {

	@GetMapping("/jaipur_view")
	public String jaipurView() {
		return "middle-body/jaipur_view";
	}
	
	@GetMapping("/rishikesh_view")
	public String rishikeshView() {
		return "middle-body/rishikesh_view";
	}
	
	@GetMapping("/shimla_view")
	public String shimlaView() {
		return "middle-body/shimla_view";
	}
	
	@GetMapping("/kolkata_view")
	public String kolkataView() {
		return "middle-body/kolkata_view";
	}
	
	@GetMapping("/darjeeling_view")
	public String darjeelingView() {
		return "middle-body/darjeeling_view";
	}
}
