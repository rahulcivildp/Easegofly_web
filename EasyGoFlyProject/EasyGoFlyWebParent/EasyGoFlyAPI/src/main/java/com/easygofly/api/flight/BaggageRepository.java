package com.easygofly.api.flight;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.BaggageOnline;

public interface BaggageRepository extends CrudRepository<BaggageOnline, Integer> {

}
