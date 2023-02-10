package com.easygofly.city;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.city.CityRepository;
import com.easygofly.entity.City;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class CityTestRepository {

	@Autowired private CityRepository repo;
	
	@Test
	public void findCityByCode() {
		City city = repo.findCityByCode("CCU");
		
		System.out.println(city.getCityName());
	}
}
