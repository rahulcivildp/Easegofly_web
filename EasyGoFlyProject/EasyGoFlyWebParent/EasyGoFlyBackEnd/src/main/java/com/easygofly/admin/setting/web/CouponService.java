package com.easygofly.admin.setting.web;

import java.util.List;
import java.util.Random;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Coupon;

@Service
@Transactional
public class CouponService {

	@Autowired CouponRepository couponRepo;
	
	public List<Coupon> listAllCoupons() {
		Iterable<Coupon> coupons = couponRepo.findAll();
		
		return (List<Coupon>) coupons;
	}
	
	public Coupon createCoupon(Integer couponAmount) {
		Coupon coupon = new Coupon();
		coupon.setCouponAmount(couponAmount);
		
		Random rand = new Random();
		int n1 = rand.nextInt(9);
		int n2 = rand.nextInt(9);
		int n3 = rand.nextInt(9);
		int n4 = rand.nextInt(9);
		int n5 = rand.nextInt(9);
		
		String generatedCode = "EGF" + n1 + "" + n2 + "" + n3 + "" + n4 + "" + n5;
		
		coupon.setCouponCode(generatedCode);
		
		return couponRepo.save(coupon);
	}
}
