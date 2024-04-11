package com.easygofly.site.hotel;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.HotelGuest;

public interface HotelGuestRepository extends CrudRepository<HotelGuest, Integer> {

}
