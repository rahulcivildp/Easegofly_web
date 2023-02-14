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

	@GetMapping("/bangalore_view")
	public String bangaloreView() {
		return "middle-body/bangalore_view";
	}
	
	@GetMapping("/kerala_view")
	public String keralaView() {
		return "middle-body/kerala_view";
	}
	
	@GetMapping("/mumbai_view")
	public String mumbaiView() {
		return "middle-body/mumbai_view";
	}
	
	@GetMapping("/visakhaptnam_view")
	public String visakhaptnamView() {
		return "middle-body/visakhaptnam_view";
	}
	
	@GetMapping("/goa_view")
	public String goaView() {
		return "middle-body/goa_view";
	}
	
	@GetMapping("/haridwar_view")
	public String haridwarView() {
		return "middle-body/haridwar_view";
	}
	
	@GetMapping("/kathmandu_view")
	public String kathmanduView() {
		return "middle-body/kathmandu_view";
	}
	
	@GetMapping("/jammu_view")
	public String jammuView() {
		return "middle-body/jammu_view";
	}
}
