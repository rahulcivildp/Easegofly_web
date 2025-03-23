package com.easygofly.api.driver;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Driver;

public interface DriverRepository extends CrudRepository<Driver, Integer> {
	
}
