package com.easygofly.site.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Transaction;

@Service
public class TransactionService {

	@Autowired TransactionRepository transactionRepo;
	
	public Transaction createTransaction(Customer customer) {
		Transaction transaction = new Transaction();
		
		transaction.setAmount("200");
		transaction.setCustomer(customer);
		return transaction;
	}
}
