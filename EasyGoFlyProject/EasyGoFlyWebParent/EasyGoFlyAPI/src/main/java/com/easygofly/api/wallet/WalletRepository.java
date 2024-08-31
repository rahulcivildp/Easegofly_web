package com.easygofly.api.wallet;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Customer;
import com.easygofly.entity.Wallet;

public interface WalletRepository extends CrudRepository<Wallet, Integer> {

	public Wallet findByCustomer(Customer customer);
}
