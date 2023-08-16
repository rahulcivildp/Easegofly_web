package com.easygofly.site.checkout;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.easygofly.entity.CartItem;
import com.easygofly.entity.Coupon;

@Service
public class CheckoutService {

	public CheckoutInfo prepareCheckout(CartItem item) {
		CheckoutInfo checkoutInfo = new CheckoutInfo();
		
		double flightCost = calculateFlightCost(item);
		float flightServiceCost = calculateFlightServiceCost(item);
		float flightGSTCost = calculateFlightGSTCost(item);
		double paymentTotal = flightCost + flightServiceCost + flightGSTCost ;
		
		checkoutInfo.setFlightCost(flightCost);
		checkoutInfo.setFlightServiceCost(flightServiceCost);
		checkoutInfo.setFlightGSTCost(flightGSTCost);
		checkoutInfo.setOrderDate(new Date());
		checkoutInfo.setPaymentTotal(paymentTotal);
		
		return checkoutInfo;
	}

	public CheckoutInfo prepareCheckoutReturn(CartItem itemOne, CartItem itemTwo) {
		CheckoutInfo checkoutInfo = new CheckoutInfo();
		
		double flightCost = calculateFlightCost(itemOne) + calculateFlightCost(itemTwo);
		float flightServiceCost = calculateFlightServiceCost(itemOne) + calculateFlightServiceCost(itemTwo);
		float flightGSTCost = calculateFlightGSTCost(itemOne) + calculateFlightGSTCost(itemTwo);
		
		double paymentTotal = flightCost + flightServiceCost + flightGSTCost;
		
		checkoutInfo.setFlightCost(flightCost);
		checkoutInfo.setFlightServiceCost(flightServiceCost);
		checkoutInfo.setFlightGSTCost(flightGSTCost);
		checkoutInfo.setOrderDate(new Date());
		checkoutInfo.setPaymentTotal(paymentTotal);
		
		return checkoutInfo;
	}
	
	public CheckoutInfo prepareCheckoutWithCoupon(CartItem item, Coupon coupon) {
		CheckoutInfo checkoutInfo = new CheckoutInfo();
		
		double flightCost = calculateFlightCost(item);
		float flightServiceCost = calculateFlightServiceCost(item);
		float flightGSTCost = calculateFlightGSTCost(item);
		double paymentTotal = flightCost + flightServiceCost + flightGSTCost - coupon.getCouponAmount() ;
		
		checkoutInfo.setFlightCost(flightCost);
		checkoutInfo.setFlightServiceCost(flightServiceCost);
		checkoutInfo.setFlightGSTCost(flightGSTCost);
		checkoutInfo.setOrderDate(new Date());
		checkoutInfo.setPaymentTotal(paymentTotal);
		
		return checkoutInfo;
	}

	private float calculateFlightGSTCost(CartItem cartItem) {
		float GSTcost = 0.0f;
		
		//GSTcost += (cartItem.getTotalPrice() * 18)/100;
		
		return GSTcost;
	}

	private float calculateFlightServiceCost(CartItem cartItem) {
		float serviceCharge = 0.0f;
		
		serviceCharge += cartItem.getServiceCost();
		
		return serviceCharge;
	}

	private double calculateFlightCost(CartItem cartItem) {
		double cost = 0.0d;
		
		cost += cartItem.getTotalPrice();
		
		return cost;
	}
}
