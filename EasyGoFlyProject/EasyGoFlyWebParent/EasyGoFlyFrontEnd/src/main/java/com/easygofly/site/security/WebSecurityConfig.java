package com.easygofly.site.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.easygofly.site.security.oauth.CustomerOAuth2UserService;
import com.easygofly.site.security.oauth.OAuth2LoginSuccessHandler;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired private CustomerOAuth2UserService oAuth2UserService;
	@Autowired private OAuth2LoginSuccessHandler oAuth2LoginHandler;
	@Autowired private DatabaseLoginSuccessHandler databaseLoginHandler;
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/", "/site-logo/**", "/customers/**", "/login", "/registration/**", "/hotel", "/about/**", 
					"/create_customer_account", "/verify", "/google5435ca7c0eebdeac.html", "/sitemap.xml",
					"/forgot_password", "/forgotPassSendEmail", "/change-pass**", "/password-save", "/flight_search_save",
					"/flight_search-noUser**", "/brand-logos/**", "/jaipur_view", "/site-logo/**", "/get_value", "/rishikesh_view", 
					"/shimla_view", "/kolkata_view", "/darjeeling_view" ).permitAll()
			.anyRequest().authenticated()
			.and()
			.formLogin()
				.loginPage("/login")
				.usernameParameter("email")
				.successHandler(databaseLoginHandler)
				.permitAll()
			.and()
			.oauth2Login()
				.loginPage("/login")
				.userInfoEndpoint()
				.userService(oAuth2UserService)
				.and()
				.successHandler(oAuth2LoginHandler)
				.permitAll()
			.and()
			.logout().permitAll()
			.and()
			.rememberMe().key("AbcDefgHijKlmnopqrst_1234567890").tokenValiditySeconds(7 * 24 * 60 * 60)
			.and();
			
	}

	@Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**", "/assets/**", "/css/**", "/style.css", "/fontawesome/**", "../brand-logos/**", "../site-logo/**");
    }
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new EasyGoFlyCustomerDetailsService();
	}
	
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}
}
