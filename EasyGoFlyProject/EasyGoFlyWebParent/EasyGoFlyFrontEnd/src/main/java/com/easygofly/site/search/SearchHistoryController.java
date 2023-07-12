package com.easygofly.site.search;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsRepository;
import com.easygofly.site.flight.ProductSaveHelper;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class SearchHistoryController {

	@Autowired private SearchHistoryService searchService;
	@Autowired private CustomerService customerService;
	@Autowired private ProductDetailService productService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private ProductDetailsRepository productRepo;
	@Autowired private CityRepository cityRepo;
	
	@GetMapping("/search_result")
	public String viewSearchResult(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		String email; 
		Customer customer;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}
				
		return "flight/search";
	}
	
	@GetMapping("/flight_search_{id}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String searchFlightDetailsSingles(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			@PathVariable(name = "totalPrice") Integer[] totalPrice, 
			Model model, RedirectAttributes redirectAttributes) {
		String email; 
		Customer customer;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}
		
		SearchHistory search = searchRepo.findById(id).get();

		System.out.println("Date: " + search.getDate());
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());
		
		searchFilter(search.getCityOne(), search.getCityTwo(), brands, model, search.getDate(), stops, totalPrice, search.getInfantNum(), activeTime);

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		Iterable<City> cities = cityRepo.findAll();
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("search", search);
		
		return "flight/search-result";
	}
	
	@GetMapping("/flight_search-noUser_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String searchFlightDetailsSinglesNoUser(
			@PathVariable(name = "cityOne") String cityOne,
			@PathVariable(name = "cityTwo") String cityTwo,
			@PathVariable(name = "journeyClass") String journeyClass,
			@PathVariable(name = "tripType") String tripType,
			@PathVariable(name = "adultNum") Integer adultNum,
			@PathVariable(name = "childNum") Integer childNum,
			@PathVariable(name = "infantNum") Integer infantNum,
			@PathVariable(name = "strDate") String strDate,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			@PathVariable(name = "totalPrice") Integer[] totalPrice,
			Model model, RedirectAttributes redirectAttributes) throws ParseException {
		 
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
	    
		System.out.println("no User Date: " + date + " - "+ sortName);

		searchSort(cityOne, cityTwo, sortName, model, date);
		
		searchFilter(cityOne, cityTwo, brands, model, date, stops, totalPrice, infantNum, activeTime);

		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = adultNum + childNum + infantNum;
		Iterable<City> cities = cityRepo.findAll();
		
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("cityOne", cityOne);
		model.addAttribute("cityTwo", cityTwo);
		model.addAttribute("date", date);
		model.addAttribute("strDate", strDate);
		model.addAttribute("journeyClass", journeyClass);
		model.addAttribute("tripType", tripType);
		model.addAttribute("adultNum", adultNum);
		model.addAttribute("childNum", childNum);
		model.addAttribute("infantNum", infantNum);
		model.addAttribute("passengerNum", passengerNum);
		model.addAttribute("brands", brands);
		model.addAttribute("stops", stops);
		model.addAttribute("totalPrice", totalPrice);
		
		return "flight/search-result-noUser";
		
	}

	private void searchFilter(String cityOne, String cityTwo, String[] brands, Model model, Date date, Integer[] stops, Integer[] totalPrice, Integer infantNum, String[] activeTime) {
		String brand1 = "";
		String brand2 = "";
		Integer stop0 = null;
		Integer stop1 = null;
		Integer stop2 = null;
		Integer stop3 = null;
		Integer priceTotal = null;
		String[] combainedTime = null;

		if (brands.length == 2 && stops.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			brand1 = brands[1];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrand(cityOne, cityTwo, date, brand1, brand2);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (brands.length == 3 && stops.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			brand1 = brands[1];
			brand2 = brands[2];  
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrand(cityOne, cityTwo, date, brand1, brand2);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (stops.length == 2 && brands.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			brand1 = "";
			brand2 = "";
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByStop(cityOne, cityTwo, date, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (stops.length == 3 && brands.length == 1 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = "";
			brand2 = "";
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByStop(cityOne, cityTwo, date, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (stops.length == 2 && brands.length == 2 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			brand1 = brands[1];
			brand2 = "";
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (stops.length == 3 && brands.length == 2 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = "";
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (stops.length == 3 && brands.length == 3 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (stops.length == 2 && brands.length == 3 && totalPrice[1] == null && activeTime.length == 1) {
			stop0 = stops[1];
			stop1 = null;
			brand1 = brands[1];
			brand2 = brands[2];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandSort(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 1 && brands.length == 1 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTotalPriceADT(cityOne, cityTwo, date, priceTotal);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 1 && brands.length == 2 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				brand1 = brands[1];
				brand2 = "";
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandTotalPriceADT(cityOne,
						cityTwo, date, brand1, brand2, priceTotal);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 2 && brands.length == 1 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByStopNumTotalPriceADT(cityOne, cityTwo, date, priceTotal, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 2 && brands.length == 2 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				brand1 = brands[1];
				brand2 = "";
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 3 && brands.length == 2 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				stop1 = stops[2];
				brand1 = brands[1];
				brand2 = "";
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 2 && brands.length == 3 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				brand1 = brands[1];
				brand2 = brands[2];
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (totalPrice[1] != null && infantNum == 0 && stops.length == 3 && brands.length == 3 && activeTime.length == 1) {
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				stop0 = stops[1];
				stop1 = stops[2];
				brand1 = brands[1];
				brand2 = brands[2];
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByBrandStopNumTotalPriceADT(cityOne, cityTwo, date, brand1, brand2, stop0, stop1, stop2, stop3, priceTotal);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] == null && brands.length == 1) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTime(cityOne, cityTwo, date, convertedArrTime1, convertedArrTime2);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] == null && brands.length == 1) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeStopNum(cityOne, cityTwo, date, stop0, stop1, stop2, stop3, convertedArrTime1, convertedArrTime2);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] == null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeBrand(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceADT(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePrice(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] == null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			stop0 = stops[1];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] == null  && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			stop0 = stops[1];
			stop1 = stops[2];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] == null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			brand2 = brands[2];
			stop0 = stops[1];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);
		} else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] == null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			brand2 = brands[2];
			stop0 = stops[1];
			stop1 = stops[2];
			List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeBrandStopNum(cityOne, cityTwo, date, brand1, brand2, convertedArrTime1, convertedArrTime2, stop0, stop1, stop2, stop3);
			model.addAttribute("listProducts", listProductDetailsBrand);

			
		}	 else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceADTBrand(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 1 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceBrand(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 1 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeStopNumPriceADT(cityOne, cityTwo, date, priceTotal, stop0, stop1, stop2, stop3, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 1) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimeStopNumPrice(cityOne, cityTwo, date, priceTotal, stop0, stop1, stop2, stop3, convertedArrTime1, convertedArrTime2);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
				
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		}  else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 2 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 2) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 3 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		} else if (activeTime.length == 2 && stops.length == 2 && totalPrice[1] != null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		}  else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 3 && infantNum == 0) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceADTBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		}  else if (activeTime.length == 2 && stops.length == 3 && totalPrice[1] != null && brands.length == 3) {
			combainedTime = activeTime[1].split(":", 2);
			float convertedArrTime1 = Float.parseFloat(combainedTime[0]);
			float convertedArrTime2 = Float.parseFloat(combainedTime[1]);
			stop0 = stops[1];
			stop1 = stops[2];
			brand1 = brands[1];
			brand2 = brands[2];
			priceTotal = totalPrice[1];
			if (priceTotal != null) {
				List<ProductDetail> listProductDetailsBrand = productService.findAllFlightsByTimePriceBrandStopNum(cityOne, cityTwo, date, priceTotal, convertedArrTime1, convertedArrTime2, brand1, brand2, stop0, stop1, stop2, stop3);
				model.addAttribute("listProducts", listProductDetailsBrand);
			}
		}
	}

	private void searchSort(String cityOne, String cityTwo, String sortName, Model model, Date date) {
		if (sortName.equals("pnr")) {
			List<ProductDetail> listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetails);
		} else if (sortName.equals("price")) {
			List<ProductDetail> listProductDetailsPrice = productService.listAllFlightsByPrice(cityOne, cityTwo, date);
			model.addAttribute("listProducts", listProductDetailsPrice);
		} else if (sortName.equals("duration")) {
			List<ProductDetail> listProductDetailsDuration = productService.listAllFlights(cityOne, cityTwo, date, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetailsDuration);
		} else if (sortName.equals("arrTime")) {
			List<ProductDetail> listProductDetailsArrival = productService.listAllFlightsByArrival(cityOne, cityTwo, date);
			model.addAttribute("listProducts", listProductDetailsArrival);
		} else if (sortName.equals("depTime")) {
			List<ProductDetail> listProductDetailsDeparture = productService.listAllFlightsByDeparture(cityOne, cityTwo, date);
			model.addAttribute("listProducts", listProductDetailsDeparture);
		} else if (sortName.equals("brand")) {
			List<ProductDetail> listProductDetailsAirline = productService.listAllFlights(cityOne, cityTwo, date, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", listProductDetailsAirline);
		}
	}
	
	@GetMapping("/get_value")
	public String somevalue(@RequestParam("cityOne") String cityOne) {
		System.out.println("City one: " + cityOne);
		return "redirect:/";
	}
	
	@GetMapping("/flight_search_save")
	public String searchHistorySave(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer,
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "cityOne", required = false) String cityOne, 
			@RequestParam(name = "cityTwo", required = false) String cityTwo, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date, 
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum,
			Model model) throws ProductNotFoundException {
		
			System.out.println("Last Value of Search: " + date);
		
			String email; 
			Customer customer;
			City city1 = cityRepo.getCityByName(cityOne);
		    City city2 = cityRepo.getCityByName(cityTwo);
		    
		    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		    String strDate = dateFormat.format(date);
		    String sort = "pnr";
		    String brand = "";
		    Integer stop = 0;
		    String activeTime = "active";
		    String arrayPrice = "0,0";
		    
			if (loggedCustomer != null) {
				email = loggedCustomer.getUsername();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				System.out.println("Last Value of Search: " + searchId);
				return "redirect:/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				model.addAttribute("customer", customer);
				Integer searchId = saveHistoryPart(city1.getCode(), city2.getCode(), date, journeyClass, tripType, adultNum, childNum,
						infantNum, customer);
				System.out.println("Last Value of Search: " + searchId);
				return "redirect:/flight_search_" + searchId +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			}else {
				return "redirect:/flight_search-noUser_"+ city1.getCode() +"_"+ city2.getCode() +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
						+"_"+ childNum +"_"+ infantNum +"_"+ strDate +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ arrayPrice +"_"+ activeTime;
			}
			
	}

	private Integer saveHistoryPart(String cityOne, String cityTwo, Date date, String journeyClass, String tripType,
			Integer adultNum, Integer childNum, Integer infantNum, Customer customer) {
		Integer totalPassenger = adultNum + childNum + infantNum;
		
		ProductSaveHelper.setSearchHistory(customer, cityOne, cityTwo, totalPassenger, journeyClass, adultNum, childNum, infantNum, tripType, date);
		
		searchService.saveSearchHistory(customer);
		Customer savedSearch = searchService.saveSearchHistory(customer);
		
		
		List<SearchHistory> savedSearchResult = savedSearch.getSearchHistory();
		SearchHistory lastValue = savedSearchResult.get(savedSearchResult.size() - 1);
		
		Integer searchId = lastValue.getId();
		return searchId;
	}
	
	@GetMapping("/process")
    @ResponseBody
    public String process() throws InterruptedException {
        // simulate a long-running process
        Thread.sleep(5000);
        return "process-complete";
    }
}
