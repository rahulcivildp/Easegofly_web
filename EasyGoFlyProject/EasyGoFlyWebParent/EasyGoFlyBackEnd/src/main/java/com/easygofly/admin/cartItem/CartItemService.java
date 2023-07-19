package com.easygofly.admin.cartItem;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.admin.order.CartItemRepository;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.ProductDetail;

@Service
public class CartItemService {
	@Autowired private CartItemRepository cartItemRepo;

	public Iterable<CartItem> updateModeOffline() {
		Iterable<CartItem> cartItems = cartItemRepo.findAll();
		for (CartItem cartItem : cartItems) {
			cartItem.setCartMode("offline");
			cartItemRepo.save(cartItem);
		}
		
		return cartItems;
	}
	
	public Iterable<CartItem> updateModeOnline() {
		Iterable<CartItem> cartItems = cartItemRepo.findAll();
		for (CartItem cartItem : cartItems) {
			ProductDetail productDetail = cartItem.getProductDetail();
			if (productDetail.getTraceId() != null ) {
				cartItem.setCartMode("online");
				cartItemRepo.save(cartItem);
			}
		}
		
		return cartItems;
	}
}
