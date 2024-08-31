package com.easygofly.api.flight;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.SeatsOnline;

public interface SeatRepository extends CrudRepository<SeatsOnline, Integer> {

}
