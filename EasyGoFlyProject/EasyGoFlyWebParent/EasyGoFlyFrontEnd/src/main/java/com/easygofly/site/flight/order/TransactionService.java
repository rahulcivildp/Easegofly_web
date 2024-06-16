package com.easygofly.site.flight.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Transaction;

@Service
public class TransactionService {

	@Autowired TransactionRepository transactionRepo;
	
	public Transaction createTransaction(Customer customer, String[] listStr) {
		Transaction transaction = new Transaction();
		
		double amount = Double.parseDouble(listStr[0]) / 100;
		
		transaction.setAmount("" + amount);
		transaction.setCustomer(customer);
		transaction.setOrderId(listStr[8]);
		transaction.setPaymentMethod(listStr[9]);
		transaction.setPaymentMode(listStr[10]);
		transaction.setResponseDescription(listStr[12]);
		transaction.setResponseCode(listStr[11]);
		transaction.setPgTransId(listStr[18]);
		transaction.setPgTransTime(listStr[19]);
		
		return transactionRepo.save(transaction);
	}
}
