package com.easygofly.site.bus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BusOtherController {

	@GetMapping("/bus/transport-apsrtc")
	public String viewAndhra(Model model) {
		
		return "bus/state/APSRTC";
	}

	@GetMapping("/bus/transport-bsrtc")
	public String viewBihar(Model model) {
		
		return "bus/state/BSRTC";
	}

	@GetMapping("/bus/transport-gsrtc")
	public String viewGujrat(Model model) {
		
		return "bus/state/GSRTC";
	}

	@GetMapping("/bus/transport-hrtc")
	public String viewHimachal(Model model) {
		
		return "bus/state/HRTC";
	}

	@GetMapping("/bus/transport-jksrtc")
	public String viewJAndK(Model model) {
		
		return "bus/state/JKSRTC";
	}

	@GetMapping("/bus/transport-ksrtc")
	public String viewKarnataka(Model model) {
		
		return "bus/state/KSRTC";
	}

	@GetMapping("/bus/transport-kerala_rtc")
	public String viewKarala(Model model) {
		
		return "bus/state/KERALA";
	}

	@GetMapping("/bus/transport-ktcl")
	public String viewGoa(Model model) {
		
		return "bus/state/KTCL";
	}

	@GetMapping("/bus/transport-nbstc")
	public String viewNorthBengal(Model model) {
		
		return "bus/state/NBSTC";
	}

	@GetMapping("/bus/transport-osrtc")
	public String viewOdisha(Model model) {
		
		return "bus/state/ORSTC";
	}

	@GetMapping("/bus/transport-rsrtc")
	public String viewRajasthan(Model model) {
		
		return "bus/state/RSRTC";
	}

	@GetMapping("/bus/transport-sbstc")
	public String viewSouthBengal(Model model) {
		
		return "bus/state/SBSTC";
	}

	@GetMapping("/bus/transport-tnstc")
	public String viewTamilNadu(Model model) {
		
		return "bus/state/TNSTC";
	}

	@GetMapping("/bus/transport-tsrtc")
	public String viewTelagana(Model model) {
		
		return "bus/state/TSRTC";
	}

	@GetMapping("/bus/transport-upsrtc")
	public String viewUttarPradesh(Model model) {
		
		return "bus/state/UPSRTC";
	}

	@GetMapping("/bus/transport-wbstc")
	public String viewWestBengal(Model model) {
		
		return "bus/state/WBSTC";
	}

	@GetMapping("/bus/transport-punbus")
	public String viewPunjab(Model model) {
		
		return "bus/state/PUNBUS";
	}
}
