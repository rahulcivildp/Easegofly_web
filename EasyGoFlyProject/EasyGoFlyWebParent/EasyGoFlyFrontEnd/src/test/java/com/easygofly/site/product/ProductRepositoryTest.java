package com.easygofly.site.product;

import static org.mockito.ArgumentMatchers.intThat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.site.EasyGoFlyFrontEndApplication;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.flight.ProductDetailsRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyFrontEndApplication.class)
public class ProductRepositoryTest {

	@Autowired 
	private ProductDetailsRepository repo;
	
	@Autowired
	private FlightRepository flightRepo;
	
	@Autowired
	private TestEntityManager enityManager;
	
	@Test
	public void testSerachByDate() {
		/*List<Product> products = repo.findProductByCity("CCU", "BLR", Sort.by("name").ascending());
		for (Product product : products) {
			String date1 = "2022-12-10 05:30:00.000000";
			List<ProductDetail> productDetail = flightRepo.findFlightByDate(new Date(),product.getId(), Sort.by("name").ascending());
		}
		*/
		//2022-12-10 05:30:00.000000
		/*Calendar c1 = Calendar.getInstance();
		  
        // set Month
        // MONTH starts with 0 i.e. ( 0 - Jan)
        c1.set(Calendar.MONTH, 12);
  
        // set Date
        c1.set(Calendar.DATE, 10);
  
        // set Year
        c1.set(Calendar.YEAR, 2022);
        
        c1.set(Calendar.HOUR, 05);
        
        c1.set(Calendar.MINUTE, 30);
        
        c1.set(Calendar.SECOND, 00);
  
        // creating a date object with specified time.
        Date dateOne = c1.getTime();
        
        List<ProductDetail> productDetail = flightRepo.findFlightByDate(dateOne, 5, Sort.by("name").ascending());
        for (ProductDetail productDetail2 : productDetail) {
			Integer id = productDetail2.getId();
			String pnr = productDetail2.getPnr();
			
			System.out.println("Flight SN.: " + id + " , Flight PNR: " + pnr);
		}*/
		Date date = new Date();
		LocalDateTime ldt = LocalDateTime.now().plusDays(1);
		DateTimeFormatter formmat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
		System.out.println(ldt);
		// Output "2018-05-12T17:21:53.658"
		//List<ProductDetail> productDetail = flightRepo.findFlightByDate(ldt, 5, Sort.by("name").ascending());
		String pattern = "MM-dd-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String dates = simpleDateFormat.format(date);
		System.out.println(dates);
		
		String formatter = formmat1.format(ldt);
		System.out.println(formatter);
		System.out.println(formmat1);
		// 2018-05-12
	}
	
}
