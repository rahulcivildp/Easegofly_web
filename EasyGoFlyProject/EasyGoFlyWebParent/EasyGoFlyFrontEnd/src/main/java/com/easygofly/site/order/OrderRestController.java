package com.easygofly.site.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Wallet;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@RestController
public class OrderRestController {
	
	@Autowired CustomerService customerService;

	@PostMapping("/check_wallet_balance")
	public String checkBalance(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, @Param("price") Double price) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			if ((wallet.getBalance() / 100) < price) {
				return "insufficient";
			}
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			if ((wallet.getBalance() / 100) < price) {
				return "insufficient";
			}
		}
				return null;
	}
}
