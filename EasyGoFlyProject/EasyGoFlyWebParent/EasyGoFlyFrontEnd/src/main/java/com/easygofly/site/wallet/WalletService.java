package com.easygofly.site.wallet;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.RechargeHistoryStatus;
import com.easygofly.entity.Wallet;

@Service
public class WalletService {

	@Autowired WalletRepository walletRepo;
	@Autowired RechargeHistoryRepository rechargeHistoryRepo;
	
	public Wallet findbyCustomer(Customer customer) {
		Wallet wallet = walletRepo.findByCustomer(customer);
		return wallet;
	}
	
	public Wallet setTempValue(Customer customer, Integer paisaValue) {
		Wallet wallet = customer.getWallet();
		wallet.setTempValue(paisaValue);
		
		return walletRepo.save(wallet);
	}
	
	public RechargeHistory createRechargeHistory(Customer customer, String transId) {
		Wallet wallet = customer.getWallet();
		
		RechargeHistory rechargeHistory = new RechargeHistory();
		rechargeHistory.setWallet(wallet);
		rechargeHistory.setRechargeAmount(wallet.getTempValue());
		rechargeHistory.setTransaction(transId);
		rechargeHistory.setDate(new Date());
		
		return rechargeHistoryRepo.save(rechargeHistory);
	}
	
	public RechargeHistory updateRechargeHistoryStatus(RechargeHistory rechargeHistory, RechargeHistoryStatus rechargeHistoryStatus) {
		rechargeHistory.setRechargeHistoryStatus(rechargeHistoryStatus);
		
		return rechargeHistoryRepo.save(rechargeHistory);
	}
	
	public Wallet updateWalletValue(Customer customer, RechargeHistory rechargeHistory) {
		Wallet wallet = customer.getWallet();
		
		wallet.setBalance(wallet.getBalance() + rechargeHistory.getRechargeAmount());
		wallet.setTempValue(0);
		
		return walletRepo.save(wallet);
	}
	
	public List<RechargeHistory> listAllRechargeHistory(Wallet wallet, Sort sort) {
		List<RechargeHistory> rechargeHistories = rechargeHistoryRepo.findByWallet(wallet, sort);
		return rechargeHistories;
	}
}
