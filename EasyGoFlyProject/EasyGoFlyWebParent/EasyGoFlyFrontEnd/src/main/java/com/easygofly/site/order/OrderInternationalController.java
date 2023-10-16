package com.easygofly.site.order;

import org.springframework.stereotype.Controller;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.easygofly.entity.BaggageOnline;
import com.easygofly.entity.Brand;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.City;
import com.easygofly.entity.Coupon;
import com.easygofly.entity.Customer;
import com.easygofly.entity.MealsOnline;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.SeatsOnline;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.entity.User;
import com.easygofly.entity.Wallet;
import com.easygofly.site.LogService;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.BrandRepositoy;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.TravelerService;
import com.easygofly.site.flight.TravellerRepository;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.search.SearchHistoryController;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;
import com.easygofly.site.wallet.WalletService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Config;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class OrderInternationalController {
	
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private CartItemService cartService;
	@Autowired private OrderService orderService;
	@Autowired private OrderRepository orderRepo;
	@Autowired private OrderController orderController;
	@Autowired private CheckoutService checkoutService;
	@Autowired private ProductDetailService productService;
	@Autowired private CityRepository cityRepo;
	@Autowired private TravellerRepository travellerRepo;
	@Autowired private CouponService couponService ;
	@Autowired private WalletService walletService;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private SearchHistoryController searchHistoryController;
	@Autowired private TravelerService travelerService;
	@Autowired private LogService logService;
	
	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;
	private Integer savedOrderId;
	private Integer savedOrderReturnId1;
	private Integer savedOrderReturnId2;
	private Integer updatedOrderId;
	private Integer updatedOrderReturnId1;
	private Integer updatedOrderReturnId2;
	private Integer hasErrorCode;
	private String hasErrorMsg;
	
	Integer search_id_inner = 0;
	Integer searchReturn_id_inner = 0;
	
	@PostMapping("/flight_international_order_save")
	public String createNewOrder(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@RequestParam(name = "couponCode") String couponCode,
			@RequestParam(name = "couponCode1") String couponCode1,
			@RequestParam(name = "totalPayment") String totalPayment,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			HttpServletRequest request, Order order3) {
		try {
			ProductDetail flight = flightRepo.findById(flightId).get();
			SearchHistory search = searchRepo.findById(searchId).get();
			CartItem item = cartRepo.findById(item_id).get();
			
			String paymentType = "PAYMENT_GATEWAY";
			PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);
			
			Date date = flight.getDate();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
			String dateTime = dateFormat.format(date);

			String orderName = flight.getCityOne() + "-" + flight.getCityTwo() + ":(" + flight.getFlightNum() + ")" + dateTime + ":" + flight.getDepTime() + "-" + flight.getArrTime() 
			+ ":(" + search.getPassengerNum() + ")";

			Order order = orderRepo.findByCartItemOrder(item_id);
			
			List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByCurtItemAndProductDetail(flight, item);

			cartService.updateCartItemOrdered(item);

			Coupon coupon = couponService.findCouponByCode(couponCode);
			Coupon coupon1 = couponService.findCouponByCode(couponCode1);
			CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
			orderController.loginControl(couponCode, couponCode1, totalPayment, loggedCustomer, googleLogin, flight, search, item,
					paymentMethod, orderName, order, travellerDetails, coupon, coupon1, checkoutInfo);
			
			return "redirect:/flight_international_order_" + search.getId() + "&" + flightId + "&" + item_id;
		} catch (Exception e) {
			return "redirect:/flight_international_order_" + searchId + "&" + flightId + "&" + item_id;
		}
	}
	
	@GetMapping("/flight_international_order_{search_id}&{flight_id}&{item_id}")
	public String orderPage(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) throws IOException {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			Double doubleAmount = (double) (wallet.getBalance() / 100);
			model.addAttribute("balance", doubleAmount);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			Double doubleAmount = (double) (wallet.getBalance() / 100);
			model.addAttribute("balance", doubleAmount);
			model.addAttribute("customer", customer);
		}
		
		ProductDetail flight = flightRepo.findById(flight_id).get();
		CartItem item = cartRepo.findById(item_id).get();
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);

		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
		Order order = orderRepo.findByCartItemOrder(item_id);
		
		if (!search_id.equals(null)) {
			SearchHistory search = searchRepo.findById(search_id).get();
			model.addAttribute("search", search);
		} else {
			SearchHistory search = searchRepo.findByCart_id(item_id);
			model.addAttribute("search", search);
		}
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
		Integer intAmount = (int) (order.getPrice() * 100);
		String amount = "" + intAmount;
		//String amount = "100";

		//Cookie cookie = request.getCookies().get("JSESSIONID");
		//String value = cookie.getValue();
		
		for (Cookie cookie : request.getCookies()) {
			if(cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}
		
		
		Transaction transaction = new Transaction();
		
		try {
			ZaakpayApiRequestParameters processPayment = transaction.processPaymentInternational(orderString, amount);
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
		}
		
		Coupon coupon = couponService.findCouponByCode(order.getCouponCode());
		
		savedOrderId = order.getId();
		
		model.addAttribute("order_id", order.getId());
		model.addAttribute("order", order);
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("flight", flight);
		model.addAttribute("search_id", search_id);
		model.addAttribute("item_id", item_id);
		model.addAttribute("coupon", coupon);
		
		return "order/international/flight_order";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/international/response",
			method = {RequestMethod.POST})
	public String zaakpayResponse (HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "search_id") Integer search_id) throws Exception {
		//com.easygofly.entity.Transaction transactions = new com.easygofly.entity.Transaction();
		search_id_inner = search_id;
		Transaction transaction = new Transaction();
	    ChecksumGenerator checksumGenerator = new ChecksumGenerator();
	    String checksumString = "" ;
	    Integer n= 0;
	    for (String param: transaction.getResponseParameters()) {
	        checksumString=checksumString+param+"="+request.getParameter(param);
	        checksumString=checksumString+"&";
	        //This will create the checksum string against every parameter.
	        parameter[n] = request.getParameter(param);
	        n+=1;
	    }
	    
	    Boolean verifyChecksum = checksumGenerator.verifyChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString,request.getParameter("checksum")) ;
	    verifiedChecksum = verifyChecksum;
	    checksum = request.getParameter("checksum");
	    responseParameters = transaction.getResponseParameters();
		
	    String orderParam = parameter[8];
		String[] parts = orderParam.split("R");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		Order order = orderRepo.findById(convert).get();
		ProductDetail productDetail = order.getProductDetail();
		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			orderService.updateOrder(order, OrderStatus.CANCELLED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			orderService.updateOrder(order, OrderStatus.FAILED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			orderService.updateOrder(order, OrderStatus.FAILED);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
			orderService.updateOrder(order, OrderStatus.SUCCESSFULL);
		} else {
			if (productDetail.getPnr().equals(null) || productDetail.getPnr().equals("")) {
				orderService.updateOrder(order, OrderStatus.PENDING);
			} else {
				orderService.updateOrder(order, OrderStatus.SUCCESSFULL);
			}
			
			Integer totalSeatRemaining = Integer.parseInt(productDetail.getTotalSeats()) - order.getPassengerNum();
			orderService.updateTotalPassenger(order, totalSeatRemaining);
		}
		
		try {
			CartItem cartItem = cartRepo.findById(order.getCartId()).get();
			if (!cartItem.equals(null)) {
				if (order.getOrderStatus().equals(OrderStatus.CANCELLED) || order.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order.getOrderStatus().equals(OrderStatus.FAILED) || order.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCurtItemAndProductDetail(productDetail, cartItem);
						for (TravellerDetail travellerDetail : travellerDetail2) {
							travellerDetail.setCartItem(null);
							travellerRepo.save(travellerDetail);
						}
							
						searchService.updateSearchHistoryCart(searchHistory, cartItem);
						searchHistory.setCartItem(null);
						searchRepo.save(searchHistory);
					}
					cartItem.setSearchHistory(null);
					cartRepo.save(cartItem);
					
					cartService.deleteCartItem(cartItem.getId());
				}
			}
		} catch (Exception e) {
			return "redirect:/zaakpay/response";
		}
		
		return "redirect:/zaakpay/response";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/international/response",
			method = {RequestMethod.GET})
	public String zaakpayResponseSe (Model model, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		
		String email; 
		Customer customer = new Customer(); 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}

		String orderParam = parameter[8];
		model.addAttribute("orderId", orderParam);
		String[] parts = orderParam.split("R");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		Order order = orderRepo.findById(convert).get();
		model.addAttribute("orderId", order.getId());
		ProductDetail productDetail = order.getProductDetail();
		
		orderController.methodSSR(productDetail);
		
		SearchHistory search = searchRepo.findById(search_id_inner).get();		
		
		if (productDetail.isLcc() == true) {
			ticketDetails(order, productDetail, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search);
		} else {
			bookingDetails(order, productDetail, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search, productDetailsController.discount, 
				productDetailsController.tdsOnIncentive, productDetailsController.tdsOnCommission, productDetailsController.tdsOnPLB, productDetailsController.otherCharges, productDetailsController.publishedFare, 
				productDetailsController.offeredFare, productDetailsController.serviceFee);
		}
		
		String pnr = productDetail.getPnr();
		model.addAttribute("pnrBarcode", pnr);
		model.addAttribute("orderStatusO", order.getOrderStatus());
		
		String cityOne = productDetail.getCityOne();
		String cityTwo = productDetail.getCityTwo();
		City city1 = cityRepo.getCityByCode(cityOne);
		City city2 = cityRepo.getCityByCode(cityTwo);
		model.addAttribute("cityOne", city1.getCityName());
		model.addAttribute("cityTwo", city2.getCityName());
		model.addAttribute("city1", cityOne);
		model.addAttribute("city2", cityTwo);
		
		Date date1 = productDetail.getDate();  
		DateFormat dateFormat1 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime = dateFormat1.format(date1);
		model.addAttribute("flightDateTime", flightDateTime);
		
		Date date2 = order.getCreatedTime();  
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime = dateFormat2.format(date2);
		model.addAttribute("orderDateTime", orderDateTime);
		
		model.addAttribute("passengerPhone", order.getPassengerNum());
		model.addAttribute("passengerEmail", order.getContactEmail());
		
		Product product = productDetail.getProduct();
		Brand brand = product.getBrands();
		model.addAttribute("brandPath", ".." + brand.getPhotosImagePath());
		model.addAttribute("brandName", brand.getName());
		model.addAttribute("productDetail", productDetail);
		model.addAttribute("originTerminal", product.getOriginTerminal());
		model.addAttribute("destinationTerminal", product.getDestinationTerminal());
		model.addAttribute("baggage", product.getBaggage());
		model.addAttribute("cabinBaggage", product.getCabinBaggage());
		
		Integer dateInt = productDetail.getDuration()/60;
		if (dateInt >= 10) {
			model.addAttribute("dateInt", dateInt);
		} else {
			model.addAttribute("dateInt", "0" + dateInt);
		}
		Integer timeInt = productDetail.getDuration()%60;
		if (timeInt >= 10) {
			model.addAttribute("timeInt", timeInt);
		} else {
			model.addAttribute("timeInt", "0" + timeInt);
		}
		
		Path flightUpPath = Paths.get("../pdf-images/flight-up.png");
		Path flightDownPath = Paths.get("../pdf-images/flight-down.png");
		Path demoTicketPath = Paths.get("../pdf-images/demo-ticket.png");
		Path thumbLogoPath = Paths.get("../pdf-images/thumb-logo.png");
		model.addAttribute("flightUpPath", flightUpPath);
		model.addAttribute("flightDownPath", flightDownPath);
		model.addAttribute("demoTicketPath", demoTicketPath);
		model.addAttribute("thumbLogoPath", thumbLogoPath);
		
		User user = product.getUser();
		model.addAttribute("user", user);
		
		if (parameter[9].contains("Not Found") && parameter[10].contains("unknown") ) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("The transaction was completed successfully.") || parameter[12].contains("Transaction has been settled.")) {
			if (hasErrorCode != null && hasErrorCode != 0) {
				model.addAttribute("paymentCancelled", OrderStatus.CANCELLED);
				System.out.println(hasErrorCode);
			} else {
				model.addAttribute("paymentSuccess", OrderStatus.SUCCESSFULL);
			}
		}
		
		
		List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
		model.addAttribute("travellerDetails", travellerDetails);
        
		Double amount = Double.parseDouble(parameter[0])/100;
		
		model.addAttribute("amount", amount);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "zaakpay/inter/response";
		
	}
	
	@PostMapping("/flight_wallet_inter_check")
	public String walletPayment(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "search_id") Integer search_id) {
		
		String email; 
		Customer customer; 
		Order order = orderRepo.findById(savedOrderId).get();
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			Wallet wallet = walletPayOrder(customer, order);
			if (wallet != null) {
				Order updatedOrder = orderUpdateWallet(order);
				updatedOrderId = updatedOrder.getId();
			}
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = walletPayOrder(customer, order);
			if (wallet != null) {
				Order updatedOrder = orderUpdateWallet(order);
				updatedOrderId = updatedOrder.getId();
			}
		}
		
		search_id_inner = search_id;
		
		return "redirect:/flight_wallet_response_international";
	}

	private Wallet walletPayOrder(Customer customer, Order order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId() + "INTER";
		return walletService.updateWalletBalanceByOrder(customer, order, orderString, "");
	}

	private Wallet walletPayOrderCancel(Customer customer, Order order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId() + "INTER";
		return walletService.cancelWalletBalanceByOrder(customer, order, orderString, "");
	}

	private Order orderUpdateWallet(Order order) {
		ProductDetail productDetail = order.getProductDetail();
		
		if (productDetail.getPnr().equals(null) || productDetail.getPnr().equals("")) {
			orderService.updateOrder(order, OrderStatus.PENDING);
		} else {
			orderService.updateOrder(order, OrderStatus.SUCCESSFULL);
		}
		
		Integer totalSeatRemaining = Integer.parseInt(productDetail.getTotalSeats()) - order.getPassengerNum();
		orderService.updateTotalPassenger(order, totalSeatRemaining);
		
		try {
			CartItem cartItem = cartRepo.findById(order.getCartId()).get();
			if (!cartItem.equals(null)) {
				if (order.getOrderStatus().equals(OrderStatus.CANCELLED) || order.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order.getOrderStatus().equals(OrderStatus.FAILED) || order.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCurtItemAndProductDetail(productDetail, cartItem);
						for (TravellerDetail travellerDetail : travellerDetail2) {
							travellerDetail.setCartItem(null);
							travellerRepo.save(travellerDetail);
						}
							
						searchService.updateSearchHistoryCart(searchHistory, cartItem);
						searchHistory.setCartItem(null);
						searchRepo.save(searchHistory);
					}
					cartItem.setSearchHistory(null);
					cartRepo.save(cartItem);
					
					cartService.deleteCartItem(cartItem.getId());
				}
			}
		} catch (Exception e) {
			return order;
		}
		return order;
	}
	
	@GetMapping("/flight_wallet_response_international")
	public String showWalletPayment(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) throws MalformedURLException, IOException {
		
		String email; 
		Customer customer = new Customer(); 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
		}
		
		Order order = orderRepo.findById(updatedOrderId).get();
		model.addAttribute("orderId", order.getId());
		ProductDetail productDetail = order.getProductDetail();

		orderController.methodSSR(productDetail);

		SearchHistory search = searchRepo.findById(search_id_inner).get();
		
		if (productDetail.isLcc() == true) {
			ticketDetails(order, productDetail, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search);
		} else {
			bookingDetails(order, productDetail, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search, productDetailsController.discount, 
				productDetailsController.tdsOnIncentive, productDetailsController.tdsOnCommission, productDetailsController.tdsOnPLB, productDetailsController.otherCharges, productDetailsController.publishedFare, 
				productDetailsController.offeredFare, productDetailsController.serviceFee);
		}
		
		String pnr = productDetail.getPnr();
		model.addAttribute("pnrBarcode", pnr);
		model.addAttribute("orderStatusO", order.getOrderStatus());
		
		String cityOne = productDetail.getCityOne();
		String cityTwo = productDetail.getCityTwo();
		City city1 = cityRepo.getCityByCode(cityOne);
		City city2 = cityRepo.getCityByCode(cityTwo);
		model.addAttribute("cityOne", city1.getCityName());
		model.addAttribute("cityTwo", city2.getCityName());
		model.addAttribute("city1", cityOne);
		model.addAttribute("city2", cityTwo);
		
		Date date1 = productDetail.getDate();  
		DateFormat dateFormat1 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime = dateFormat1.format(date1);
		model.addAttribute("flightDateTime", flightDateTime);
		
		Date date2 = order.getCreatedTime();  
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime = dateFormat2.format(date2);
		model.addAttribute("orderDateTime", orderDateTime);
		
		model.addAttribute("passengerPhone", order.getPassengerNum());
		model.addAttribute("passengerEmail", order.getContactEmail());
		
		String photoImagePath = "";
		Brand brand = brandRepo.getBrandByName(productDetail.getBrand().toLowerCase());
		if (brand.equals(null)) {
			photoImagePath = brand.getPhotosImagePath();
		} else {
			photoImagePath = "#";
		}
		model.addAttribute("brandPath", ".." + photoImagePath);
		model.addAttribute("brandName", brand.getName());
		model.addAttribute("productDetail", productDetail);
		model.addAttribute("originTerminal", productDetail.getTerminalDep());
		model.addAttribute("destinationTerminal", productDetail.getTerminalArr());
		model.addAttribute("baggage", productDetail.getBaggage());
		model.addAttribute("cabinBaggage", productDetail.getCabinBaggage());
		
		Integer dateInt = productDetail.getDuration()/60;
		if (dateInt >= 10) {
			model.addAttribute("dateInt", dateInt);
		} else {
			model.addAttribute("dateInt", "0" + dateInt);
		}
		Integer timeInt = productDetail.getDuration()%60;
		if (timeInt >= 10) {
			model.addAttribute("timeInt", timeInt);
		} else {
			model.addAttribute("timeInt", "0" + timeInt);
		}
		
		Path flightUpPath = Paths.get("../pdf-images/flight-up.png");
		Path flightDownPath = Paths.get("../pdf-images/flight-down.png");
		Path demoTicketPath = Paths.get("../pdf-images/demo-ticket.png");
		Path thumbLogoPath = Paths.get("../pdf-images/thumb-logo.png");
		model.addAttribute("flightUpPath", flightUpPath);
		model.addAttribute("flightDownPath", flightDownPath);
		model.addAttribute("demoTicketPath", demoTicketPath);
		model.addAttribute("thumbLogoPath", thumbLogoPath);
		
//		User user = product.getUser();
//		model.addAttribute("user", user);
		
		List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
		model.addAttribute("travellerDetails", travellerDetails);
		if (hasErrorCode != null && hasErrorCode != 0) {
			model.addAttribute("paymentCancelled", hasErrorMsg);
			walletPayOrderCancel(customer, order);
			System.out.println(hasErrorCode);
		} else {
			model.addAttribute("paymentSuccess", "successfull");
		}
		
		model.addAttribute("amount", order.getPrice());
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "wallet/inter/response";
	}
	
	private void bookingDetails(Order order, ProductDetail productDetail, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId, String discount, String tdsOnIncentive, String tdsOnCommission, String tdsOnPLB, String otherCharges, 
			 String publishedFare, String offeredFare, String serviceFee ) throws IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
		
		if (!productDetail.getTraceId().equals("offline")) {
			
			String[] airlineNoArray = productDetail.getFlightNum().split("-");
			String isLeadPax = "false";
			Integer countAdult = 0;
		
			for (TravellerDetail travellerDetail : travelers) {
				BaggageOnline baggageOnline = travellerDetail.getBaggageOnline();
				MealsOnline mealsOnline = travellerDetail.getMealOnline();
				SeatsOnline seatsOnline = travellerDetail.getSeatsOnline();
				
   			Date getDOB = travellerDetail.getDob();
   			Date getPassportExpiry = travellerDetail.getPassportExpiry();
   			Integer genNum = 0;
   			if (travellerDetail.getSalutation().equals("Mr") || travellerDetail.getSalutation().equals("Mstr")) {
   				genNum = 1;
   			} else {
   				genNum = 2;
   			}
   			String baseFare = "";
   			String tax = "";
   			
   			//baggage information
   			String bagCode = baggageOnline.getCode(), bagWeight = baggageOnline.getWeight(), bagPrice = baggageOnline.getPrice();
   			
   			//meal information
   			String mealCode = mealsOnline.getCode(), mealName = mealsOnline.getName(), mealQuantity = mealsOnline.getQuantity(), mealPrice = mealsOnline.getPrice();
   			
   			//seat information
   			@SuppressWarnings("unused")
				String seatAvailabilityType = seatsOnline.getAvailablityType().toString(), seatCode = seatsOnline.getCode(), seatRowNo = seatsOnline.getRowNo(), 
   					seatNo = seatsOnline.getSeatNo(), seatType = seatsOnline.getSeatType().toString(), seatDeck = seatsOnline.getDeck().toString(), seatCompartment = seatsOnline.getCompartment().toString(), 
   					seatPrice = seatsOnline.getPrice(), seatCraftType = seatsOnline.getCraftType();

   			
   			if (travellerDetail.getPaxType().equals("1")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
   				Integer taxInt = Integer.parseInt(taxTravelerAdult) / searchId.getAdultNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
   				
   				if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
   				countAdult++;
   				System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
   				Integer taxInt = Integer.parseInt(taxTravelerChild) / searchId.getChildNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
   				Integer taxInt = Integer.parseInt(taxTravelerInfant) / searchId.getInfantNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}
   			
   			String details = "{\r\n"
   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
   					+ "		\"Gender\": " + genNum + ",\r\n"
   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
   					+ "		\"PassportExpiry\": \"" + getPassportExpiry + "T00:00:00\",\r\n"
   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
   					+ "		\"AddressLine2\": \"\",\r\n"
   					+ "		\"Fare\": {\r\n"
   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
   					+ "			\"Tax\": " + tax + ",\r\n"
   					+ "			\"YQTax\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
   					+ "			\"OtherCharges\": " + otherCharges + ",\r\n"
   					+ "			\"Discount\": " + discount + ",\r\n"
   					+ "			\"PublishedFare\": " + publishedFare + ",\r\n"
   					+ "			\"OfferedFare\": " + offeredFare + ",\r\n"
   					+ "			\"TdsOnCommission\": " + tdsOnCommission + ",\r\n"
   					+ "			\"TdsOnPLB\": " + tdsOnPLB + ",\r\n"
   					+ "			\"TdsOnIncentive\": " + tdsOnIncentive + ",\r\n"
   					+ "			\"ServiceFee\": " + serviceFee + "\r\n"
   					+ "		},\r\n"
   					+ "		\"City\": \"Gurgaon\",\r\n"
   					+ "		\"CountryCode\": \"IN\",\r\n"
   					+ "		\"CountryName\": \"India\",      \r\n"
   					+ "     \"Nationality\": \"IN\",\r\n"
   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
   					+ "		\"FFAirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
   					+ "		\"FFNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyName\": \"\",\r\n"
   					+ "		\"GSTNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
   					+ "}";
   			
   			travelerDetailsArray.add(details);

				
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	URL urlBook = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Book");
           // Open a connection
           HttpURLConnection connectionBook = (HttpURLConnection) urlBook.openConnection();
           
           StringBuilder responseBodyBook = new StringBuilder();
           
       	onlineFlightService.apiOnlineBookingNonLCC(connectionBook, responseBodyBook, searchHistoryController.traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	JSONObject jsonObjBooking = new JSONObject(responseBodyBook.toString()); 
       	System.out.println(jsonObjBooking);
       	logService.generateLog(jsonObjBooking.toString());
       	
       	String bookingId = "", pnrBooking = "";
       	Integer bookingIdInt = 0;

		List<String> passportListArray = new ArrayList<String>();
       	
       	try {
				JSONObject jsonObjBookingResponse = jsonObjBooking.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
					pnrBooking = jsonObjBookingResponse.get("PNR").toString();
					bookingId  = jsonObjBookingResponse.get("BookingId").toString();
					bookingIdInt = Integer.parseInt(bookingId);
				
					for (int i = 0; i < jsonObjBookingResponse.getJSONArray("Passenger").length(); i++) {
						JSONObject jsonObjPassengers = jsonObjBookingResponse.getJSONArray("Passenger").getJSONObject(i);
						
						String paxId = jsonObjPassengers.get("PaxId").toString();
						Integer paxIdInt = Integer.parseInt(paxId);
						String passportNo = jsonObjPassengers.get("PassportNo").toString();
						String passportExpiry = jsonObjPassengers.get("PassportExpiry").toString();
						String dateOfBirth = jsonObjPassengers.get("DateOfBirth").toString();
						
						
						String passportList = "{\r\n"
								+ "    \"PaxId\": " + paxIdInt + ",\r\n"
								+ "    \"PassportNo\": \"" + passportNo + "\",\r\n"
								+ "    \"PassportExpiry\": \"" + passportExpiry + "\",\r\n"
								+ "    \"DateOfBirth\": \"" + dateOfBirth + "\"\r\n"
								+ "  }";
						
						passportListArray.add(passportList);
					}
					
					String arrayPassportList = passportListArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));
					
					/* Ticket details */
			       	URL urlTicket = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Ticket");
			           // Open a connection
			           HttpURLConnection connectionTicket = (HttpURLConnection) urlTicket.openConnection();
			           
			           StringBuilder responseBodyTicket = new StringBuilder();
			           
			       	onlineFlightService.apiOnlineTicketNonLccPassport(connectionTicket, responseBodyTicket, searchHistoryController.traceId, pnrBooking, bookingIdInt, arrayPassportList);
			       	
			       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
			       	System.out.println(jsonObjTicket);
			       	logService.generateLog(jsonObjTicket.toString());
			       	
			       	try {
						JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
						JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
//	       	JSONObject jsonObjectSegments = new JSONObject();
						
						String terminalDep = "", terminalArr = "";
						for (int i = 0; i < jsonArraySegment.length(); i++) {
							JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
							if (i == 0) {
								terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
							}
							if (i == (jsonArraySegment.length() - 1)) {
								terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
							}
						}
						
						String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
						String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

						productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
						productService.updatePNROnline(productDetail, onlinePNR);
						productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
						orderService.updateBookingId(order, onlineBookingId);

						hasErrorCode = 0;
						hasErrorMsg = "";
					} catch (JSONException json) {
						JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
						
						hasErrorCode = Integer.parseInt(jsonObjTicketResponseError.get("ErrorCode").toString());
						hasErrorMsg = jsonObjTicketResponseError.get("ErrorMessage").toString();
						
					} 
				
			} catch (JSONException json) {
				JSONObject jsonObjTicketResponseError = jsonObjBooking.getJSONObject("Response").getJSONObject("Error");
				
				hasErrorCode = Integer.parseInt(jsonObjTicketResponseError.get("ErrorCode").toString());
				hasErrorMsg = jsonObjTicketResponseError.get("ErrorMessage").toString();
				System.out.println("Not found!!");
				
			}
       	

		}
	}
	
	public void ticketDetails(Order order, ProductDetail productDetail, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId ) throws MalformedURLException, IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
		
		if (!productDetail.getTraceId().equals("offline")) {
			
			String[] airlineNoArray = productDetail.getFlightNum().split("-");
			String isLeadPax = "false";
			Integer countAdult = 0;
		
			for (TravellerDetail travellerDetail : travelers) {
				BaggageOnline baggageOnline = travellerDetail.getBaggageOnline();
				MealsOnline mealsOnline = travellerDetail.getMealOnline();
				SeatsOnline seatsOnline = travellerDetail.getSeatsOnline();
				
   			Date getDOB = travellerDetail.getDob();
   			Date getPassportExpiry = travellerDetail.getPassportExpiry();
   			Integer genNum = 0;
   			if (travellerDetail.getSalutation().equals("Mr") || travellerDetail.getSalutation().equals("Mstr")) {
   				genNum = 1;
   			} else {
   				genNum = 2;
   			}
   			String baseFare = "";
   			String tax = "";
   			
   			//baggage information
   			String bagCode = baggageOnline.getCode(), bagWeight = baggageOnline.getWeight(), bagPrice = baggageOnline.getPrice();
   			
   			//meal information
   			String mealCode = mealsOnline.getCode(), mealName = mealsOnline.getName(), mealQuantity = mealsOnline.getQuantity(), mealPrice = mealsOnline.getPrice();
   			
   			//seat information
   			@SuppressWarnings("unused")
				String seatAvailabilityType = seatsOnline.getAvailablityType().toString(), seatCode = seatsOnline.getCode(), seatRowNo = seatsOnline.getRowNo(), 
   					seatNo = seatsOnline.getSeatNo(), seatType = seatsOnline.getSeatType().toString(), seatDeck = seatsOnline.getDeck().toString(), seatCompartment = seatsOnline.getCompartment().toString(), 
   					seatPrice = seatsOnline.getPrice(), seatCraftType = seatsOnline.getCraftType();
   			
   			
   			String baggageDetailsString = "		\"Baggage\":[\r\n"
   					+ "            {\r\n"
   					+ "                \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "                \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ "                \"WayType\": 2,\r\n"
   					+ "                \"Code\": \"" + bagCode + "\",\r\n"
   					+ "                \"Description\": 2,\r\n"
   					+ "                \"Weight\": " + bagWeight + ",\r\n"
   					+ "                \"Currency\": \"INR\",\r\n"
   					+ "                 \"Price\": " + bagPrice + ",\r\n"
   					+ "                 \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
   					+ "				}\r\n"
   					+ "			],\r\n";
   			
   			String mealDetailsString = "     \"MealDynamic\": [\r\n"
   					+ "        {\r\n"
   					+ "          \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "          \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ "          \"WayType\": 2,\r\n"
   					+ "          \"Code\": \"" + mealCode + "\",\r\n"
   					+ "          \"Description\": 2,\r\n"
   					+ "          \"AirlineDescription\": \"" + mealName + "\",\r\n"
   					+ "          \"Quantity\": " + mealQuantity + ",\r\n"
   					+ "          \"Currency\": \"INR\",\r\n"
   					+ "          \"Price\": " + mealPrice + ",\r\n"
   					+ "          \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
   					+ "          \"Destination\": \"" + productDetail.getCityTwo() + "\"\r\n"
   					+ "        }],\r\n";
   			
   			String seatDetailsString = "		\"SeatDynamic\": [\r\n"
   					+ "        {\r\n"
   					+ "	    \"AirlineCode\": \"" + airlineNoArray[0] + "\",\r\n"
   					+ "             \"FlightNumber\": \"" + airlineNoArray[1] + "\",\r\n"
   					+ "              \"CraftType\": \"" + productDetail.getCraftType() + "\",\r\n"
   					+ "               \"Origin\": \"" + productDetail.getCityOne() + "\",\r\n"
   					+ "                \"Destination\": \"" + productDetail.getCityTwo() + "\",\r\n"
   					+ "                \"AvailablityType\": " + seatAvailabilityType + ",\r\n"
   					+ "                \"Description\": 2,\r\n"
   					+ "                \"Code\": \"" + seatCode + "\",\r\n"
   					+ "                \"RowNo\": \"" + seatRowNo + "\",\r\n"
   					+ "                \"SeatNo\": \"" + seatNo + "\",\r\n"
   					+ "                \"SeatType\": " + seatType + ",\r\n"
   					+ "                \"SeatWayType\": 2,\r\n"
   					+ "                \"Compartment\": " + seatCompartment + ",\r\n"
   					+ "                \"Deck\": " + seatDeck + ",\r\n"
   					+ "                \"Currency\": \"INR\",\r\n"
   					+ "                \"Price\": " + seatPrice + "                                                                                                                                                                                                      \r\n"
   					+ "			\r\n"
   					+ "		}],\r\n";
   			
   			String baggageDetails = baggageDetailsString;
   			String mealDetails = mealDetailsString;
   			String seatDetails = seatDetailsString;
   			
   			if (travellerDetail.getPaxType().equals("1")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerAdult) / searchId.getAdultNum();
   				Integer taxInt = Integer.parseInt(taxTravelerAdult) / searchId.getAdultNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
   				
   				if (countAdult == 0) {
						isLeadPax = "true";
					} else {
						isLeadPax = "false";
					}
   				countAdult++;
   				System.out.println(countAdult);
				} else if (travellerDetail.getPaxType().equals("2")) {
   				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
   				Integer taxInt = Integer.parseInt(taxTravelerChild) / searchId.getChildNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				} else {
   				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
   				Integer taxInt = Integer.parseInt(taxTravelerInfant) / searchId.getInfantNum();
   				baseFare = "" + fareInt;
   				tax = "" + taxInt;
   				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
					isLeadPax = "false";
				}
   			
   			String details = "{\r\n"
   					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
   					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
   					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
   					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
   					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
   					+ "		\"Gender\": " + genNum + ",\r\n"
   					+ "		\"PassportNo\": \"" + travellerDetail.getPassportNo() + "\",\r\n"
   					+ "		\"PassportExpiry\": \"" + getPassportExpiry + "T00:00:00\",\r\n"
   					+ "		\"AddressLine1\": \"123, Test\",\r\n"
   					+ "		\"AddressLine2\": \"\",\r\n"
   					+ "		\"Fare\": {\r\n"
   					+ "			\"BaseFare\": " + baseFare + ",\r\n"
   					+ "			\"Tax\": " + tax + ",\r\n"
   					+ "			\"YQTax\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeePub\": 0.0,\r\n"
   					+ "			\"AdditionalTxnFeeOfrd\": 0.0,\r\n"
   					+ "			\"OtherCharges\": 0.0\r\n"
   					+ "		},\r\n"
   					+ "		\"City\": \"Gurgaon\",\r\n"
   					+ "		\"CountryCode\": \"IN\",\r\n"
   					+ "		\"CountryName\": \"India\",      \r\n"
   					+ "     \"Nationality\": \"IN\",\r\n"
   					+ "		\"ContactNo\": \"" + order.getPhoneNumber() + "\",\r\n"
   					+ "		\"Email\": \"" + order.getContactEmail() + "\",\r\n"
   					+ "		\"IsLeadPax\": " + isLeadPax + ",\r\n"
   					+ "		\"FFAirlineCode\": \"" + productDetailsController.airlineCOde + "\",\r\n"
   					+ "		\"FFNumber\": \"" + productDetailsController.flightNumber + "\",\r\n"
   					+ baggageDetails
   					+ mealDetails
   					+ seatDetails
   					+ "		\"GSTCompanyAddress\": \"\",\r\n"
   					+ "		\"GSTCompanyContactNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyName\": \"\",\r\n"
   					+ "		\"GSTNumber\": \"\",\r\n"
   					+ "		\"GSTCompanyEmail\": \"\"\r\n"
   					+ "}";
   			
   			travelerDetailsArray.add(details);

				
   		}
       	
       	String arrayTraveler = travelerDetailsArray.stream().map(val -> String.valueOf(val)).collect(Collectors.joining(",", "[", "]"));

       	/* Ticket details */
       	URL urlTicket = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/Ticket");
           // Open a connection
           HttpURLConnection connectionTicket = (HttpURLConnection) urlTicket.openConnection();
           
           StringBuilder responseBodyTicket = new StringBuilder();
           
       	onlineFlightService.apiOnlineTicket(connectionTicket, responseBodyTicket, searchHistoryController.traceId, productDetail.getResultIndex(), arrayTraveler);
       	
       	
       	JSONObject jsonObjTicket = new JSONObject(responseBodyTicket.toString()); 
       	System.out.println(jsonObjTicket);
       	logService.generateLog(jsonObjTicket.toString());
       	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
//       	JSONObject jsonObjectSegments = new JSONObject();
				
				String terminalDep = "", terminalArr = "";
				for (int i = 0; i < jsonArraySegment.length(); i++) {
					JSONObject jsonObjectSegments = jsonArraySegment.getJSONObject(i);
					if (i == 0) {
						terminalDep = jsonObjectSegments.getJSONObject("Origin").getJSONObject("Airport").get("Terminal").toString();
					}
					if (i == (jsonArraySegment.length() - 1)) {
						terminalArr = jsonObjectSegments.getJSONObject("Destination").getJSONObject("Airport").get("Terminal").toString();
					}
				}
				
				String onlinePNR = jsonObjTicketResponse.get("PNR").toString();
				String onlineBookingId = jsonObjTicketResponse.get("BookingId").toString();

				productService.updateOtherDetails(productDetail, terminalDep, terminalArr);
				productService.updatePNROnline(productDetail, onlinePNR);
				productService.setTotalSeatOnline(productDetail, productDetail.getUploadSeats());
				orderService.updateBookingId(order, onlineBookingId);
				
				/* Get Booking Details */
				URL urlGetBookingDetails = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/GetBookingDetails");
				// Open a connection
				HttpURLConnection connectionGetBookingDetails = (HttpURLConnection) urlGetBookingDetails.openConnection();
				
				StringBuilder responseBodyGetBookingDetails = new StringBuilder();
				
				onlineFlightService.apiOnlineGetBookingDetails(connectionGetBookingDetails, responseBodyGetBookingDetails, searchHistoryController.traceId, onlinePNR, onlineBookingId);
				
				@SuppressWarnings("unused")
				JSONObject jsonObjGetBookingDetails = new JSONObject(responseBodyGetBookingDetails.toString());

				hasErrorCode = 0;
				hasErrorMsg = "";
			} catch (JSONException json) {
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				
				hasErrorCode = Integer.parseInt(jsonObjTicketResponseError.get("ErrorCode").toString());
				hasErrorMsg = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
			} 

		}
	}

}
