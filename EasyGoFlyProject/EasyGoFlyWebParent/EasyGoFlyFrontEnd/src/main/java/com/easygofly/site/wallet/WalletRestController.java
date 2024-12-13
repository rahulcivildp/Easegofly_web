package com.easygofly.site.wallet;

import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Wallet;

@RestController
public class WalletRestController {

	@GetMapping("/wallet-balance-show")
	public double responseBalance(@Param("customer") Customer customer) {
		Wallet wallet = customer.getWallet();
		double INRbalance = wallet.getBalance() / 100;
		System.out.println(INRbalance + "dddddddddddddddddddddddddddddd");
		
		return INRbalance;
	}
}
