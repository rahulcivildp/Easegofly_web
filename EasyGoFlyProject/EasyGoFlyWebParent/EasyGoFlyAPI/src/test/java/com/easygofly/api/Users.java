package com.easygofly.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.api.security.PasswordEncoderConfig;


@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyApiApplication.class)
public class Users {
	private PasswordEncoderConfig passwordEncoder;

	@Test
	public void testUpdateAuthenticationType() {
		String encodedPass = PasswordEncoderConfig.passwordEncoder().encode("12345678");
		
		System.out.println("Encoded password: " + encodedPass);
	}
}
