package com.easygofly.setting;
 
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.state.StateRepository;
import com.easygofly.entity.Country;
import com.easygofly.entity.State;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class StateRepositoryTest {

	@Autowired
	private StateRepository stateRepo;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	public void testCreateStateInIndia() {
		Integer countryId = 1;
		Country country = entityManager.find(Country.class, countryId);
		
		State state = stateRepo.save(new State("West Bengal", country));
		
		assertThat(state).isNotNull();
		assertThat(state.getId()).isGreaterThan(0);
	}
}
