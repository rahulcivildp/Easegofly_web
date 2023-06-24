package com.easygofly.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@Lazy 
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new EasyGoFlyUserDetailsService();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/favicon/**").permitAll()
			.antMatchers("/users/**", "/settings/**").hasAuthority("Admin")
			.antMatchers("/categories/**", "/brands/**").hasAnyAuthority("Admin", "Editor")
			.antMatchers("/products/new", "/products/save", "/products/delete/**").hasAnyAuthority("Admin", "Editor")
			.antMatchers("/products/edit/**", "/products/check_name").hasAnyAuthority("Admin", "Editor", "Salesperson")
			.antMatchers("/products", "/products/", "/products/detail/**", "/products/page/**").hasAnyAuthority("Admin", "Editor", "Salesperson", "Shipper")
			.antMatchers("/products/**").hasAnyAuthority("Admin", "Editor")
			.anyRequest().authenticated()
			.and()
			.formLogin().loginPage("/login").usernameParameter("email").permitAll()
			.and()
			.logout().permitAll()
			.and()
			.rememberMe().key("AbcDefgHijKlmnopqrst_1234567890").tokenValiditySeconds(7 * 24 * 60 * 60)
			.and();
	}

	@Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**", "../favicon/**");
    }
	
}
