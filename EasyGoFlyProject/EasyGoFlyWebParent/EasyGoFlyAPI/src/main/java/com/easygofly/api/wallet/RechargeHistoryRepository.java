package com.easygofly.api.wallet;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.Wallet;

public interface RechargeHistoryRepository extends CrudRepository<RechargeHistory, Integer> {

	@Query("SELECT r FROM RechargeHistory r WHERE r.wallet = :wallet")
	public List<RechargeHistory> findByWallet(Wallet wallet, Sort sort);
}
