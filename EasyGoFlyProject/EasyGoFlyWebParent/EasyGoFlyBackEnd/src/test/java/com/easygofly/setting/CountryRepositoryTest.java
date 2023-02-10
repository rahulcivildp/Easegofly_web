package com.easygofly.setting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.CountryRepository;
import com.easygofly.entity.Country;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class CountryRepositoryTest {

	@Autowired
	private CountryRepository countryRepo;
	

	
	@Test
	public void testCreateCountry() {
		
		Country country = new Country("United States", "USA");
		Country savedCountry = countryRepo.save(country);
		
		assertThat(country).isNotNull();
		assertThat(savedCountry.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateState() {
		
		
	}
}
