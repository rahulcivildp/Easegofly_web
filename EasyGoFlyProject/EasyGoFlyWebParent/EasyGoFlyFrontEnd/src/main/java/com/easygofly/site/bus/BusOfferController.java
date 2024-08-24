package com.easygofly.site.bus;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BusOfferController {
	
	@GetMapping("/bus/transport_tamilnadu")
	public String transportTamilnadu() {
		return "bus/offers/tamilnadu-transport";
	}
	
	@GetMapping("/bus/transport_ahmedabad")
	public String transportAhmedabad() {
		return "bus/offers/ahmedabad-transport";
	}
	
	@GetMapping("/bus/transport_bangalore")
	public String transportBangalore() {
		return "bus/offers/bangalore-transport";
	}
	
	@GetMapping("/bus/transport_delhi")
	public String transportDelhi() {
		return "bus/offers/delhi-transport";
	}
	
	@GetMapping("/bus/transport_hyderabad")
	public String transportHyderabad() {
		return "bus/offers/hyderabad-transport";
	}
	
	@GetMapping("/bus/transport_mumbai")
	public String transportMumbai() {
		return "bus/offers/mumbai-transport";
	}
	
	@GetMapping("/bus/transport_pune")
	public String transportPune() {
		return "bus/offers/pune-transport";
	}

	@GetMapping("/bus/transport_andhra_pradesh")
	public String transportAP() {
		return "bus/offers/ap-transport";
	}
	
	@GetMapping("/bus/transport_kolkata")
	public String transportKolkata() {
		return "bus/offers/kolkata-transport";
	}
	
	@GetMapping("/bus/transport_odisha")
	public String transportOdisha() {
		return "bus/offers/odisha-transport";
	}
	
	@GetMapping("/bus/transport_bihar")
	public String transportBihar() {
		return "bus/offers/bihar-transport";
	}
	
	@GetMapping("/bus/transport_uttar_pradesh")
	public String transportUP() {
		return "bus/offers/up-transport";
	}
	
}
