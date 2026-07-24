package com.easygofly.category;
 
import static org.assertj.core.api.Assertions.assertThat;

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
public class CityRepositoryTest {

	@Autowired
	private CityRepository repo;
	
	@Test
	public void testCreateNewCities() {
		City cities = new City("Kolkata", "CCU");
		City savedCity = repo.save(cities);
		
		assertThat(cities).isNotNull();
		assertThat(savedCity.getId()).isGreaterThan(0);
			
	}
}
