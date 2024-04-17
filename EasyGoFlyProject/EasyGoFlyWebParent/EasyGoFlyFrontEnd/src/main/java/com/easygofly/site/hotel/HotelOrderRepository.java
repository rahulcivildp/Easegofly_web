package com.easygofly.site.hotel;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.HotelOrder;

public interface HotelOrderRepository extends CrudRepository<HotelOrder, Integer> {

}
