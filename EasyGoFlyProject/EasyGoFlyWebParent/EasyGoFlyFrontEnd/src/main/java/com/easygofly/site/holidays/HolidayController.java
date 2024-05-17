package com.easygofly.site.holidays;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HolidayController {


	@GetMapping("/holiday")
	public String viewBusPage(Model model) {
		
		return "holiday/holiday";
	}

}
