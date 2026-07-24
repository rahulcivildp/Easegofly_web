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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.CountryRepository;
import com.easygofly.entity.Country;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@DataJpaTest
@SpringBootTest
@AutoConfigureMockMvc
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class CountryRestControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	CountryRepository repo;
	
	@Test
	@WithMockUser(username = "nam@codejava.net", password = "nam2020", roles = "ADMIN")
	public void testListCountries() throws Exception {
		String url = "/countries/list";
		MvcResult result = mockMvc.perform(get(url))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();
		
		String jsonResponse = result.getResponse().getContentAsString();
		System.out.println(jsonResponse);
		
		Country[] countries = objectMapper.readValue(jsonResponse, Country[].class);
		for (Country country : countries) {
			System.out.println(country);
		}
	}
	
	@Test
	@WithMockUser(username = "nam@codejava.net", password = "nam2020", roles = "ADMIN")
	public void testCreateCountries() throws JsonProcessingException, Exception {
		String url = "/countries/save";
		String countryName = "Germany";
		String countryCode = "DE";
		Country country = new Country(countryName, countryCode);
		
		MvcResult result = mockMvc.perform(post(url).contentType("application/json").content(objectMapper.writeValueAsString(country)).with(csrf()))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();
		
		String response = result.getResponse().getContentAsString();
		Integer countryId = Integer.parseInt(response);
		
		Optional<Country> findCountryById = repo.findById(countryId);
		assertThat(findCountryById.isPresent());
		
		Country savedCountry = findCountryById.get();
		
		//System.out.println("Country ID: " + response);
		assertThat(savedCountry.getName()).isEqualTo(countryName);
	}
}
