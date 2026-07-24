package com.easygofly.setting;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.CountryRepository;
import com.easygofly.admin.setting.state.StateRepository;
import com.easygofly.entity.Country;
import com.easygofly.entity.State;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class StateRestRepositoryTest {

	@Autowired MockMvc mockMvc;
	
	@Autowired ObjectMapper objectMapper;
	
	@Autowired CountryRepository countryRepo;
	
	@Autowired StateRepository stateRepo;
	
	@Test
	@WithMockUser(username = "nam", password = "something", roles = "ADMIN")
	public void testListByCountries() throws Exception {
		Integer countryId = 1;
		String url = "/states/list_by_country/" + countryId;
		
		MvcResult result = mockMvc.perform(get(url))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();
		
		String jsonResponse = result.getResponse().getContentAsString();
		State[] states = objectMapper.readValue(jsonResponse, State[].class);
		
		assertThat(states).hasSizeGreaterThan(0);
	}
	
	@Test
	@WithMockUser(username = "nam", password = "something", roles = "ADMIN")
	public void testCreateState() throws Exception {
		String url = "/states/save";
		Integer countryId = 3;
		Country country = countryRepo.findById(countryId).get();
		State state = new State("Arizona", country);
		
		MvcResult result = mockMvc.perform(post(url).contentType("application/json")
				.content(objectMapper.writeValueAsString(state))
				.with(csrf()))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();
		
		String response = result.getResponse().getContentAsString();
		Integer stateId = Integer.parseInt(response);
		Optional<State> findById = stateRepo.findById(stateId);
		
		assertThat(findById.isPresent());
	}
}
