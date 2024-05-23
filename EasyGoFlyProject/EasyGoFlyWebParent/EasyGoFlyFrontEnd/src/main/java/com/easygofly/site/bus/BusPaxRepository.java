package com.easygofly.site.bus;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.BusPassenger;

public interface BusPaxRepository extends CrudRepository<BusPassenger, Integer> {

}
