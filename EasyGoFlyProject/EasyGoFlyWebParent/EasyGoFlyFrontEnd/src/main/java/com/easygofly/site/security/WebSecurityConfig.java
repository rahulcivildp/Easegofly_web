package com.easygofly.site.security;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.easygofly.site.security.oauth.CustomerOAuth2UserService;
import com.easygofly.site.security.oauth.OAuth2LoginSuccessHandler;


@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Autowired private BeforeAuthenticationFilter beforeLoginFilter;
    @Autowired private LoginSuccessHandler loginSuccessHandler;
    @Autowired private LoginFailureHandler loginFailureHandler;
    @Autowired private CustomerOAuth2UserService oAuth2UserService;
    @Autowired private OAuth2LoginSuccessHandler auth2LoginHandler;

    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("*")); // add this line with appropriate methods for your case
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
	
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
	    return new HttpSessionEventPublisher();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception { 
		http.csrf().disable();
		
		http.authorizeRequests()
			.antMatchers("/", "/favicon/**", "/site-logo/**", "/brand-logos/**", "/images/**", "/images/bus/**", "/customers/**", "/login**", "/registration", "/registration/**", "/hotel", "/about/**", "/customer-photos/**",
					"/create_customer_account", "/verify", "/google5435ca7c0eebdeac.html", "/sitemap.xml", "/indirect_login**", "/indirect_login/**", "/driver-photos/**", "/cab-photos/**",
					"/forgot_password", "/forgotPassSendEmail", "/change-pass**", "/password-save", "/flight_search_save", "/flight_search/return_**",  "/flight_search**",
					"/flight_international_search_save", "/flight_international_search_return_save", "/flight_search_**", "/brand-logos/**", "/jaipur_view", "/site-logo/**", 
					"/get_value", "/rishikesh_view", "/shimla_view", "/kolkata_view", "/darjeeling_view", "/bangalore_view", "/kerala_view", "/mumbai_view", "/flight_sort",
					"/visakhaptnam_view", "/goa_view", "/haridwar_view", "/kathmandu_view", "/jammu_view", "/flight_booking**", "/flight_booking/return_**",
					"/loading", "/process", "/flight_activity**", "/authentication", "/loading_**", "/api_results", "/find_brand_**", "/cab/order/api**", "/zaakpay/cab/response/api", "/cab/order/api_new_",
					"/save_meal", "/test", "/save_timer**", "/find_city_name_**", "/find_city_by_code_**", "/flight", "/traveller_details**",
					"/hotel/saveSearchHotel", "/hotel/search_**", "/hotel_loading...", "/bus", "/bus/saveSearchBus", "/bus_loading...", "/bus/search_**", "/holiday", "/bus/test_response", 
					"/bus/transport-**", "/test_wallet_send_email", "/testing", "/get_flight_list", "/get_least_fare_by_brand", "/get_next_day_flight",
					"/get_previous_day_flight", "/sort_flights_by_**", "/noUser_search_filter", "/mode", "/bus/transport_**", "/coming_soon", "/test_redirect", "/flight_redirect", 
					"/assets/images/**", "/contact-us", "/wallet-recharge/api**", "/wallet-confirm/api**", "/zaakpay/recharge/api", "/api/flight/tbo-search/**", "/api/fetch_cities", "/api/brand_list",
					"/api/flight/leastFare", "/api/flight/tbo-search/calendar-fare", "/show_hotel_city", "/flight-payment/api/**", "/zaakpay/flight/response/api**").permitAll()
			.anyRequest().authenticated()
            .and()
            .addFilterBefore(beforeLoginFilter,
                    BeforeAuthenticationFilter.class)
			.formLogin()
				.loginPage("/login")
				.usernameParameter("phone")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
				.permitAll()
			.and()
            .addFilterBefore(beforeLoginFilter,
                    BeforeAuthenticationFilter.class)
			.formLogin()
				.loginPage("/login")
				.usernameParameter("email")
	            .successHandler(loginSuccessHandler)
	            .failureHandler(loginFailureHandler)
				.permitAll()
			.and()
            .oauth2Login()
	        	.loginPage("/login")
	        	.userInfoEndpoint()
	        	.userService(oAuth2UserService)
	            .and()
	        	.successHandler(auth2LoginHandler)
	        .and()
			.logout().permitAll()
			.and()
			.rememberMe().key("AbcDefgHijKlmnopqrst_1234567890").tokenValiditySeconds(7 * 24 * 60 * 60)
			.and()
			.sessionManagement()
				.sessionFixation().migrateSession()
				.maximumSessions(2)
				.expiredUrl("/login");
		
			http.cors();
	} 
	
	@Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**", "/assets/**", "/assets/js/**", "/assets/css/**", "/assets/images/**", "/css/**", "/style.css", "/fontawesome/**", 
        		"/fontawesome/all.css", "../brand-logos/**", "../site-logo/**", "../favicon/**", "../customer-photos/**", "/driver-photos/**", "/cab-photos/**");
    } 

	
	public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/**")
	    .allowedMethods("GET", "POST", "OPTIONS")
	    .allowedOrigins("https://easegofly.com/")
	    .allowCredentials(true);
	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new EasegoflyPhoneCustomerDetailsService();
	}
	
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}

	
}
