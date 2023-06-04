package com.easygofly.admin.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.admin.customer.CustomerRepository;
import com.easygofly.admin.customer.CustomerService;
import com.easygofly.admin.customer.WalletRepository;
import com.easygofly.entity.Wallet;

@RestController
public class CustomerRestController {
	
	@Autowired CustomerRepository customerRepo;
	@Autowired CustomerService customerService ;
	@Autowired WalletRepository walletRepo;

	@PostMapping("/add_balance_{id}_{addedBalance}")
	public String addBalance(@PathVariable("id") Integer id, @PathVariable("addedBalance") Integer addedBalance) {
		Wallet wallet = walletRepo.findById(id).get();
		Integer intTotalAmount = wallet.getBalance() + (addedBalance * 100);
		Integer intAmount = (addedBalance * 100);
		
		wallet.setBalance(intTotalAmount);
		Wallet savedWallet = walletRepo.save(wallet);
		
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "W"+ savedWallet.getId();
		
		System.out.println("TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest" + id + "   -   " + addedBalance);
		customerService.createRechargeHistory(savedWallet.getCustomer(), orderString, intAmount);
		
		return "" + savedWallet.getBalance();
	}
}
