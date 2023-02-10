package com.easygofly.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.Role;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.customer.RoleRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class RoleRepositoryTest {

	@Autowired
	private RoleRepository roleRepo;
	
	@Test
	public void testCreateRestRole() {
		Role roleAdmin = new Role("Admin", "Manages Evreything");
		Role roleEditor = new Role("Editor", "Manage categories, brands, products, articles and menus");
		Role roleCustomer = new Role("Customer", "View products, view orders and update order status");
		Role roleGeneral = new Role("General", "View only");
		
		roleRepo.saveAll(List.of(roleAdmin, roleEditor, roleCustomer, roleGeneral));
	}
	
	@Test
	public void testPasswordEncoder() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String rawPassword = "12345678";
		String encodedPassword = passwordEncoder.encode(rawPassword);
		String anotherPassword = "$2a$10$w3e6bpOzPnBzXvqMdV.ecu.MfTJGvtUIQgXZB9HXXonLAn2UcTAnS";
		
		System.out.println(encodedPassword);
		
		boolean matches = passwordEncoder.matches(rawPassword, anotherPassword);
		
		assertThat(matches).isTrue();
	}
}
