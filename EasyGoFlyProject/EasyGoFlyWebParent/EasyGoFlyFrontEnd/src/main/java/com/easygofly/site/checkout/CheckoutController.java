package com.easygofly.site.checkout;


import com.cashfree.lib.constants.Constants.Environment;
import com.cashfree.lib.payout.clients.Payouts;
import com.easygofly.site.setting.SettingService;

import javax.servlet.http.HttpServletRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {
	
	@Autowired private SettingService settingService;
	
	@PostMapping("/order_creation")
	public String newOrder(HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String orderId = null;
		Payouts payouts = Payouts.getInstance(Environment.PRODUCTION, "126820c8661cf1da082fa5f2c0028621", "3b28f3b4abd21b4e023e126b2b4ff5ae27fbea7e");

		payouts.init();
		
		System.out.println("Order ID: " + orderId);
		return "payment/razorpay";
	}
	
}
