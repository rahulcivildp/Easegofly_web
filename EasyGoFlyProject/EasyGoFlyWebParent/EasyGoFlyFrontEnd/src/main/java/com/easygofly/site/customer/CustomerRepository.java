package com.easygofly.site.customer;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.easygofly.entity.AuthenticationType;
import com.easygofly.entity.Customer;

public interface CustomerRepository extends PagingAndSortingRepository<Customer, Integer> {
	@Query("SELECT c FROM Customer c WHERE c.email = :email")
	public Customer getCustomerByEmail(@Param("email") String email);
	
	public Long countById(Integer id);
	
	@Query("SELECT c FROM Customer c WHERE c.VerificationCode = ?1")
	public Customer getCustomerByVerificationCode(String code);
	
	@Query("SELECT c FROM Customer c WHERE c.resetPasswordToken = ?1")
	public Customer getCustomerByResetPasswordToken(String token);
	
	@Query("UPDATE Customer c SET c.enabled = true, c.VerificationCode = null WHERE c.id = ?1")
	@Modifying
	public void updateEnableStatus(Integer id);
	
	@Query("SELECT c FROM Customer c WHERE c.email = ?1")
	public Customer findCustomerByEmail(String email);
	
	@Query("UPDATE Customer c SET c.authenticationType = ?2 WHERE c.id = ?1")
	@Modifying
	public void updateAuthenticationType(Integer customerId, AuthenticationType type);
}
