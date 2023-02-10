package com.easygofly.admin.order;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.CartItem;

public interface CartItemRepository extends CrudRepository<CartItem, Integer> {

}
