package com.easygofly.site.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Coupon;

@Service
public class CouponService {

	@Autowired private CouponRepository couponRepo;
	
	public Coupon findCouponByCode(String couponCode) {
		Coupon coupon = couponRepo.findByCouponCode(couponCode);
		return coupon;
	}
}
