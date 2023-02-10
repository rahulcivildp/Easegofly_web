package com.easygofly.admin.setting;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Country;

public interface CountryRepository extends CrudRepository<Country, Integer> {

	public List<Country> findAllByOrderByNameAsc();
}
