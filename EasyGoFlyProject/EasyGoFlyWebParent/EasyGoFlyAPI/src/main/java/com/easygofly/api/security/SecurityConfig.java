package com.easygofly.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}
	
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(PasswordEncoderConfig.passwordEncoder());
		
		return authProvider;
	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new EasegoflyPhoneCustomerDetailsService();
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		// TODO Auto-generated method stub
		return super.authenticationManagerBean();
	}
  

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		 http
         .csrf().disable()
         .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         .and()
         .authorizeRequests()
         .antMatchers("/login").permitAll()
         .anyRequest().authenticated()
         .and()
         .addFilter(new JwtAuthenticationFilter(authenticationManager()))
         .addFilter(new JwtAuthorizationFilter(authenticationManager()));
	}

}
