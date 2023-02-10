package com.easygofly.currency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.setting.CurrencyRepository;
import com.easygofly.entity.Currency;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class CurrencyRepositoryTests {
	
	@Autowired
	CurrencyRepository repo;
	
	@Test
	public void testCreateCurrency() {
		Currency australian = new Currency(1, "Australian Dollar", "$", "AUD");
		Currency brazilian = new Currency(2, "Brazilian Real", "R$", "BRL");
		Currency british = new Currency(3, "British Pound", "£", "GBP");
		Currency canadian = new Currency(4, "Canadian Dollar", "$", "CAD");
		Currency chinese = new Currency(5, "Chinese Yuan", "¥", "CNY");
		Currency euro = new Currency(6, "Euro", "€", "EUR");
		Currency indian = new Currency(7, "Indian Rupees", "₹", "INR");
		Currency japanese = new Currency(8, "Japanese Yen", "¥", "JPY");
		Currency russian = new Currency(9, "Russian Ruble", "₽‎", "RUB");
		Currency southKorean = new Currency(10, "South Korean Won", "₩", "KRW");
		Currency unitedStates = new Currency(11, "United States Dollar", "$", "USD");
		Currency vietnamese = new Currency(12, "Vietnamese Dong", "₫", "VND");
		
		repo.saveAll(List.of(australian, brazilian, british, canadian, chinese, euro, indian, japanese, russian, southKorean, unitedStates, vietnamese));
		Iterable<Currency> findAll = repo.findAll();
		
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testListAllSortedByName() {
		List<Currency> currencies = repo.findAllByOrderByNameAsc();
		
		currencies.forEach(System.out::println);
		
		assertThat(currencies.size()).isGreaterThan(0);
	}
}
