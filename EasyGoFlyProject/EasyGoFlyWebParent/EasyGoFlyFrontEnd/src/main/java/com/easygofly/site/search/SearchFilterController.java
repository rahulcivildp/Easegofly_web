package com.easygofly.site.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.entity.City;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.ProductDetailsRepository;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;

@Controller
public class SearchFilterController {
	@Autowired private SearchHistoryController searchHistoryController;
	@Autowired private ProductDetailsController productDetailsController ;
	@Autowired private ProductDetailsRepository productRepo;
	@Autowired private CityRepository cityRepo;
	@Autowired private CustomerService customerService;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private ProductDetailService productService;

	public List<ProductDetail> productDetails = new ArrayList<ProductDetail>();
	public List<ProductDetail> originalProductDetails;
	
	@PostMapping("/noUser_search_filter")
	public String searchFilter(
			@RequestParam(name = "brand") String brand,
			@RequestParam(name = "stop") String stop,
			@RequestParam(name = "totalPrice", required = false) String totalPrice,
			@RequestParam(name = "cityOne") String cityOne,
			@RequestParam(name = "cityTwo") String cityTwo, 
			@RequestParam(name = "date", required = false) String date,
			@RequestParam(name = "passengerNum", required = false) Integer passengerNum,
			@RequestParam(name = "journeyClass", required = false) String journeyClass,
			@RequestParam(name = "tripType", required = false) String tripType,
			@RequestParam(name = "activeTime", required = false) String activeTime,
			@RequestParam(name = "adultNum", required = false) Integer adultNum,
			@RequestParam(name = "childNum", required = false) Integer childNum,
			@RequestParam(name = "infantNum", required = false) Integer infantNum, Model model) {
		String[] arrayBrand = brand.split("\\,");
		String[] arrayStop = stop.split("\\,");
		String[] arrayTotalPrice = totalPrice.split("\\,");
		String brandName = "", stopName = arrayStop[0];
		Float totalpriceInt = 0f;
		if (arrayBrand.length == 2) {
			brandName = arrayBrand[1];
		}
		if (arrayStop.length == 2) {
			stopName = arrayStop[1];
		}
		System.out.println(brand);
		System.out.println(stop);
		System.out.println(totalPrice);
		
		originalProductDetails = productDetailsController.listProductDetailsOnline;
		filterFlights(adultNum, childNum, infantNum, arrayStop, arrayTotalPrice, brandName, stopName);
		
	    String sort = "pnr";
	    return "redirect:/flight_search-noUser_filter_"+ cityOne +"_"+ cityTwo +"_"+ journeyClass +"_"+ tripType +"_"+ adultNum 
	    		+"_"+ childNum +"_"+ infantNum +"_"+ date +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ totalPrice +"_"+ activeTime;
	}

	private void filterFlights(Integer adultNum, Integer childNum, Integer infantNum, String[] arrayStop,
			String[] arrayTotalPrice, String brandName, String stopName) {
		Float totalpriceInt;
		productDetails = new ArrayList<ProductDetail>();
		
		for (ProductDetail productDetail : originalProductDetails) {
			totalpriceInt = (productDetail.getPriceADT() * ( adultNum + childNum )) + (productDetail.getPriceINF() * infantNum);
			if (productDetail.getBrand().toLowerCase().equals(brandName) && (Float.parseFloat(arrayTotalPrice[1]) == 15000 && stopName.equals(arrayStop[0]))) {
				System.out.println("2222222222222222222");
				System.out.println(productDetail.getBrand().toLowerCase());
				productDetails.add(productDetail);
			}
			if (productDetail.getStopNum() == Integer.parseInt(stopName) && (Float.parseFloat(arrayTotalPrice[1]) == 15000 && brandName.equals(""))) {
//				System.out.println("3333333333333333333");
				productDetails.add(productDetail);
			} 
			if ((totalpriceInt  < Float.parseFloat(arrayTotalPrice[1])) && (Float.parseFloat(arrayTotalPrice[1]) != 15000) && (stopName.equals(arrayStop[0]) && brandName.equals(""))) {
//				System.out.println("4444444444444444444");
				productDetails.add(productDetail);
			}
			if (productDetail.getBrand().toLowerCase().equals(brandName) && productDetail.getStopNum() == Integer.parseInt(stopName) && Float.parseFloat(arrayTotalPrice[1]) == 15000) {
//				System.out.println("555555555555555555");
				System.out.println(productDetail.getBrand().toLowerCase());
				productDetails.add(productDetail);
			}
			if (productDetail.getBrand().toLowerCase().equals(brandName) && (totalpriceInt  < Float.parseFloat(arrayTotalPrice[1])) && (Float.parseFloat(arrayTotalPrice[1]) != 15000) && stopName.equals(arrayStop[0])) {
//				System.out.println("666666666666666666");
				System.out.println(productDetail.getBrand().toLowerCase());
				productDetails.add(productDetail);
			}
			if (productDetail.getStopNum() == Integer.parseInt(stopName) && (totalpriceInt  < Float.parseFloat(arrayTotalPrice[1])) && (Float.parseFloat(arrayTotalPrice[1]) != 15000) && brandName.equals("")) {
//				System.out.println("77777777777777777");
				System.out.println(productDetail.getBrand().toLowerCase());
				productDetails.add(productDetail);
			}
			if (productDetail.getBrand().toLowerCase().equals(brandName) && productDetail.getStopNum() == Integer.parseInt(stopName) && (totalpriceInt  < Float.parseFloat(arrayTotalPrice[1])) && (Float.parseFloat(arrayTotalPrice[1]) != 15000)) {
//				System.out.println("888888888888888888");
				System.out.println(productDetail.getBrand().toLowerCase());
				productDetails.add(productDetail);
			}
		}
	}
	
	@PostMapping("/user_search_filter")
	public String userSearchFilter(
			@RequestParam(name = "brand") String brand,
			@RequestParam(name = "stop") String stop,
			@RequestParam(name = "totalPrice", required = false) String totalPrice,
			@RequestParam(name = "searchId") String searchId, 
			@RequestParam(name = "date", required = false) String date,
			@RequestParam(name = "activeTime", required = false) String activeTime, Model model) {
		
		String[] arrayBrand = brand.split("\\,");
		String[] arrayStop = stop.split("\\,");
		String[] arrayTotalPrice = totalPrice.split("\\,");
		String brandName = "", stopName = arrayStop[0];
		Float totalpriceInt = 0f;
		if (arrayBrand.length == 2) {
			brandName = arrayBrand[1];
		}
		if (arrayStop.length == 2) {
			stopName = arrayStop[1];
		}
		System.out.println(brand);
		System.out.println(stop);
		System.out.println(totalPrice);
		
		SearchHistory search = searchRepo.findById(Integer.parseInt(searchId)).get();
		
		originalProductDetails = productDetailsController.listProductDetailsOnline;
		filterFlights(search.getAdultNum(), search.getChildNum(), search.getInfantNum(), arrayStop, arrayTotalPrice, brandName, stopName);
	
	    String sort = "pnr";
	    return "redirect:/filter_flight_search_" + Integer.parseInt(searchId) +"_"+ sort +"_"+ brand +"_"+ stop +"_"+ totalPrice +"_"+ activeTime;
	}
	
	@GetMapping("/filter_flight_search_{id}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
	public String searchFlightDetailsSingles(@PathVariable(name = "id") Integer id, SearchHistory searchHistory, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			@PathVariable(name = "sortName") String sortName,
			@PathVariable(name = "activeTime") String[] activeTime,
			@PathVariable(name = "brand") String[] brands,
			@PathVariable(name = "stop") Integer[] stops,
			@PathVariable(name = "totalPrice") Integer[] totalPrice, 
			Model model, RedirectAttributes redirectAttributes) throws MalformedURLException, IOException {
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
		
		searchSort(search.getCityOne(), search.getCityTwo(), sortName, model, search.getDate());

		List<Product> getProductBrand = productRepo.findProductByCity(search.getCityOne(), search.getCityTwo(), Sort.by("name").ascending());
		
		Iterable<City> cities = cityRepo.findAll();
		
		model.addAttribute("listProducts", productDetails);
		model.addAttribute("cities", cities);
		model.addAttribute("getProductBrand", getProductBrand);
		model.addAttribute("search", search);
		
		return "flight/search/search-result";
	}
	
	@GetMapping("/flight_search-noUser_filter_{cityOne}_{cityTwo}_{journeyClass}_{tripType}_{adultNum}_{childNum}_{infantNum}_{strDate}_{sortName}_{brand}_{stop}_{totalPrice}_{activeTime}")
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
			Model model, RedirectAttributes redirectAttributes) throws ParseException, IOException {
		 
	    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);

		searchSort(cityOne, cityTwo, sortName, model, date);

		List<Product> getProductBrand = productRepo.findProductByCity(cityOne, cityTwo, Sort.by("name").ascending());
		
		Integer passengerNum = adultNum + childNum + infantNum;
		Iterable<City> cities = cityRepo.findAll();

		model.addAttribute("listProducts", productDetails);
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
		
		return "flight/search/search-result-noUser";
		
	}
	
	private void searchSort(String cityOne, String cityTwo, String sortName, Model model, Date date) {
		String traceIdstr = "offline";
		if (sortName.equals("pnr")) {
			searchHistoryController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdstr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", searchHistoryController.listProductDetails);
		} else if (sortName.equals("price")) {
			searchHistoryController.listProductDetails = productService.listAllFlightsByPrice(cityOne, cityTwo, date, traceIdstr);
			model.addAttribute("listProducts", searchHistoryController.listProductDetails);
		} else if (sortName.equals("duration")) {
			searchHistoryController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdstr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", searchHistoryController.listProductDetails);
		} else if (sortName.equals("arrTime")) {
			searchHistoryController.listProductDetails = productService.listAllFlightsByArrival(cityOne, cityTwo, date, traceIdstr);
			model.addAttribute("listProducts", searchHistoryController.listProductDetails);
		} else if (sortName.equals("depTime")) {
			searchHistoryController.listProductDetails = productService.listAllFlightsByDeparture(cityOne, cityTwo, date, traceIdstr);
			model.addAttribute("listProducts", searchHistoryController.listProductDetails);
		} else if (sortName.equals("brand")) {
			searchHistoryController.listProductDetails = productService.listAllFlights(cityOne, cityTwo, date, traceIdstr, Sort.by(sortName).ascending());
			model.addAttribute("listProducts", searchHistoryController.listProductDetails);
		}
	}
}
