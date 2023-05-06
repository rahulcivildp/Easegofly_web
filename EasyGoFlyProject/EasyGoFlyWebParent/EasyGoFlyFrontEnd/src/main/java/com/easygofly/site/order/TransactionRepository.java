package com.easygofly.site.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easygofly.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

	
}
