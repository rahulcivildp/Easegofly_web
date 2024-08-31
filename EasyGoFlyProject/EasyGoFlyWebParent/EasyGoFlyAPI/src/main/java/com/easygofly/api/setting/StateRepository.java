package com.easygofly.api.setting;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Country;
import com.easygofly.entity.State;

public interface StateRepository extends CrudRepository<State, Integer> {

	public List<State> findByCountryOrderByNameAsc(Country country);
}
