package com.easygofly.site.checkout;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.CheckoutInfo;


public interface CheckOutRepository extends CrudRepository<CheckoutInfo, Integer> {

}
