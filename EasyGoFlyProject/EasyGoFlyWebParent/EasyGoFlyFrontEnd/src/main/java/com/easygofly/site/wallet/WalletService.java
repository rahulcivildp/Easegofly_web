package com.easygofly.site.wallet;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
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
	
	public Wallet updateWalletBalanceByOrder(Customer customer, Order order, String transId) {
		Wallet wallet = customer.getWallet();
		Integer intOrder100 = (int) order.getPrice() * 100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() - intOrder100);
			wallet.setTempValue(intOrder100);
			
			RechargeHistory rechargeHistory = createRechargeHistory(customer, transId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);
			
			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}
	
	public Wallet updateWalletBalanceByOrderReturn(Customer customer, Order order1, Order order2, String transId) {
		Wallet wallet = customer.getWallet();
		Integer intOrder100 = ((int) order1.getPrice() + (int) order2.getPrice()) * 100;
		Integer intOrder = ((int) order1.getPrice() + (int) order2.getPrice());
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() - intOrder100);
			wallet.setTempValue(intOrder100);
			
			RechargeHistory rechargeHistory = createRechargeHistory(customer, transId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);
			
			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}
}
