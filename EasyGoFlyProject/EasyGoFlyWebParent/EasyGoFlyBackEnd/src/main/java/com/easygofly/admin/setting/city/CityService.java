package com.easygofly.admin.setting.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easygofly.entity.City;
import com.easygofly.entity.Order;

@Service
public class CityService {

	@Autowired private CityRepository cityRepo;
	
	public City findCityOneByCode(Order order) {
		City city = cityRepo.findCityByCode(order.getCityOne());
		
		return city;
	}
	
	public City findCityTwoByCode(Order order) {
		City city = cityRepo.findCityByCode(order.getCityTwo());
		
		return city;
	}
}
