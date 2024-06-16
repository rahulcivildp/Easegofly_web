package com.easygofly.site.flight.order;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Coupon;

public interface CouponRepository extends CrudRepository<Coupon, Integer> {
	
	@Query("SELECT c FROM Coupon c WHERE c.couponCode = ?1")
	public Coupon findByCouponCode(String couponCode);
	
}
