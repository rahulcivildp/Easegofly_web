package com.easygofly.site.order;

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

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
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
import com.easygofly.entity.Category;
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
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.BrandRepositoy;
import com.easygofly.site.flight.CityRepository;
import com.easygofly.site.flight.CityService;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.flight.ProductDetailsController;
import com.easygofly.site.flight.TravelerService;
import com.easygofly.site.flight.TravellerRepository;
import com.easygofly.site.flightAPI.OnlineFlightService;
import com.easygofly.site.order.exporter.OrderPDFExporter;
import com.easygofly.site.search.SearchHistoryController;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.search.SearchHistoryService;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.GeneralSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;
import com.easygofly.site.wallet.WalletService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Config;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class OrderController {
	
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private SearchHistoryService searchService;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private CartItemService cartService;
	@Autowired private OrderService orderService;
	@Autowired private OrderRepository orderRepo;
	@Autowired private CheckoutService checkoutService;
	@Autowired private ProductDetailService productService;
	@Autowired private SettingService settingService;
	@Autowired private CityRepository cityRepo;
	@Autowired private CityService cityService;
	@Autowired private TravellerRepository travellerRepo;
	@Autowired private CouponService couponService ;
	@Autowired private WalletService walletService;
	@Autowired private ProductDetailsController productDetailsController;
	@Autowired private OnlineFlightService onlineFlightService;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private EntityManager entityManager;
	@Autowired private SearchHistoryController searchHistoryController;
	@Autowired private TravelerService travelerService;
	
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
	
	@PostMapping("/flight_order_save")
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
			loginControl(couponCode, couponCode1, totalPayment, loggedCustomer, googleLogin, flight, search, item,
					paymentMethod, orderName, order, travellerDetails, coupon, coupon1, checkoutInfo);
			
			return "redirect:/flight_order_" + search.getId() + "&" + flightId + "&" + item_id;
		} catch (Exception e) {
			return "redirect:/flight_order_" + searchId + "&" + flightId + "&" + item_id;
		}
	}

	private void loginControl(String couponCode, String couponCode1, String totalPayment,
			EasyGoFlyCustomerDetails loggedCustomer, CustomerOAuth2User googleLogin, ProductDetail flight,
			SearchHistory search, CartItem item, PaymentMethod paymentMethod, String orderName, Order order,
			List<TravellerDetail> travellerDetails, Coupon coupon, Coupon coupon1, CheckoutInfo checkoutInfo) {
		String email;
		Customer customer;
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			if (coupon1 != null) {
				CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon1);
				if (order != null) {
					orderService.updateOrderPrice(order, checkoutInfo1);
					orderService.addCouponCode(order, couponCode1);
				}
				
				Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
				orderService.addCouponCode(order2, couponCode1);
				for (TravellerDetail travellerDetail : travellerDetails) {
					orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
				}
			} else if (coupon != null) {
				CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon);
				if (order != null) {
					orderService.updateOrderPrice(order, checkoutInfo1);
					orderService.addCouponCode(order, couponCode);
				}
				
				Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
				orderService.addCouponCode(order2, couponCode);
				for (TravellerDetail travellerDetail : travellerDetails) {
					orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
				}
			} else if (order != null) {
				if (flight.getTraceId().equals("offline")) {
					orderService.updateOrderPrice(order, checkoutInfo);
					orderService.deleteCouponCode(order);
				} else {
					orderService.updateOrderPriceOnline(order, travellerDetails, checkoutInfo);
					orderService.deleteCouponCode(order);
				}
				
			} else {
				if (flight.getTraceId().equals("offline")) {
					Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer, travellerDetails);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
					orderService.deleteCouponCode(order2);
				} else {
					Order order2 = orderService.createOrderOnline(customer, item, flight, paymentMethod, totalPayment, search, orderName, travellerDetails);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
					orderService.deleteCouponCode(order2);
				}	
			}
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			if (coupon1 != null) {
				CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon1);
				if (order != null) {
					orderService.updateOrderPrice(order, checkoutInfo1);
					orderService.addCouponCode(order, couponCode1);
				}
				
				Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
				orderService.addCouponCode(order2, couponCode1);
				for (TravellerDetail travellerDetail : travellerDetails) {
					orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
				}
			} else if (coupon != null) {
				CheckoutInfo checkoutInfo1 = checkoutService.prepareCheckoutWithCoupon(item, coupon);
				if (order != null) {
					orderService.updateOrderPrice(order, checkoutInfo1);
					orderService.addCouponCode(order, couponCode);
				}
				
				Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo1, orderName, order, customer, travellerDetails);
				orderService.addCouponCode(order2, couponCode);
				for (TravellerDetail travellerDetail : travellerDetails) {
					orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
				}
			} else if (order != null) {
				if (flight.getTraceId().equals("offline")) {
					orderService.updateOrderPrice(order, checkoutInfo);
					orderService.deleteCouponCode(order);
				} else {
					orderService.updateOrderPriceOnline(order, travellerDetails, checkoutInfo);
					orderService.deleteCouponCode(order);
				}
			} else {
				if (flight.getTraceId().equals("offline")) {
					Order order2 = saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer, travellerDetails);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
					orderService.deleteCouponCode(order2);
				} else {
					Order order2 = orderService.createOrderOnline(customer, item, flight, paymentMethod, totalPayment, search, orderName, travellerDetails);
					for (TravellerDetail travellerDetail : travellerDetails) {
						orderService.updateTravelersOrderId(travellerDetail.getId(), order2);
					}
					orderService.deleteCouponCode(order2);
				}	
			}
		}
	}

	private Order saveOrderCreate(ProductDetail flight, SearchHistory search, CartItem item, PaymentMethod paymentMethod,
			CheckoutInfo checkoutInfo, String orderName, Order order, Customer customer, List<TravellerDetail> travellerDetails) {
		if (order == null) {
			return orderService.createOrder(customer, item, flight, paymentMethod, checkoutInfo, search, orderName, travellerDetails);
		}else if (item.getId() != order.getCartId()) {
			return orderService.createOrder(customer, item, flight, paymentMethod, checkoutInfo, search, orderName, travellerDetails);
		} else {
			return null;
		}
	}
	
	@GetMapping("/flight_order_{search_id}&{flight_id}&{item_id}")
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
			ZaakpayApiRequestParameters processPayment = transaction.processPayment(orderString, amount);
			
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
		
		return "order/flight_order";
	}
	
	/**/
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/response",
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
	@RequestMapping(value = "/zaakpay/response",
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
		
		methodSSR(productDetail);
		
		SearchHistory search = searchRepo.findById(search_id_inner).get();		
		
		ticketDetails(order, productDetail, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search);
		
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
		
		return "zaakpay/response";
		
		
	}

	private void methodSSR(ProductDetail productDetail) throws MalformedURLException, IOException {
		/* SSR details */
    	URL urlSSR = new URL("http://api.tektravels.com/BookingEngineService_Air/AirService.svc/rest/SSR");
        // Open a connection
        HttpURLConnection connectionSSR = (HttpURLConnection) urlSSR.openConnection();
        
        StringBuilder responseBodySSR = new StringBuilder();
        
    	onlineFlightService.apiOnlineFarerule_quote(connectionSSR, responseBodySSR, searchHistoryController.traceId, productDetail.getResultIndex());
    	
    	JSONObject jsonObjSSR = new JSONObject(responseBodySSR.toString()); 
    	System.out.println("jsonObjSSR" + jsonObjSSR);
	}

	@GetMapping("/order/export_pdf/{id}")
	public void exportToPDF(HttpServletResponse response, @PathVariable("id") Integer id) throws Exception {
		Order order = orderRepo.findById(id).get();
		ProductDetail productDetail = order.getProductDetail();
		OrderPDFExporter exporter = new OrderPDFExporter();
		City city1 = cityService.findCityOneByCode(order);
		City city2 = cityService.findCityTwoByCode(order);
		GeneralSettingBag settingBag = settingService.getGeneralSettingBag();
		String logoLink = settingBag.getSiteLogo();
		String faviconLink = settingBag.getFavicon();
		Brand brand = brandRepo.getBrandByName(productDetail.getBrand());
		Category category = entityManager.find(Category.class, 1);
		
		if (brand == null) {
			brand = new Brand(productDetail.getBrand());
			brand.addCategory(category);
		} 
		
		List<TravellerDetail> travellers = travellerRepo.findTravellerByProductDetailAndOrder(productDetail, order);
		
		exporter.export(order, response, city1, city2, logoLink, travellers, faviconLink, brand); 

	}
	
	
	////Flight Return Segment
	
	@PostMapping("/flight_order_return_save")
	public String createNewOrderReturn(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flightOne_id") Integer flightOne_id, 
			@RequestParam(name = "itemOne_id") Integer itemOne_id, 
			@RequestParam(name = "flightTwo_id") Integer flightTwo_id, 
			@RequestParam(name = "itemTwo_id") Integer itemTwo_id,
			@RequestParam(name = "couponCode") String couponCode,
			@RequestParam(name = "couponCode1") String couponCode1,
			@RequestParam(name = "totalPayment1") String totalPayment1,
			@RequestParam(name = "totalPayment2") String totalPayment2,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			HttpServletRequest request, Order order3) {
		try {
			
			SearchHistory search = searchRepo.findById(searchId).get();
			ProductDetail flightOne = flightRepo.findById(flightOne_id).get();
			CartItem itemOne = cartRepo.findById(itemOne_id).get();
			ProductDetail flightTwo = flightRepo.findById(flightTwo_id).get();
			CartItem itemTwo = cartRepo.findById(itemTwo_id).get();
			
			String returnTypeOne = "R1";
			String returnTypeTwo = "R2";
			
			String paymentType = "PAYMENT_GATEWAY";
			PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);
			Date dateOne = flightOne.getDate();
			Date dateTwo = flightTwo.getDate();
			Coupon coupon = couponService.findCouponByCode(couponCode);
			Coupon coupon1 = couponService.findCouponByCode(couponCode1);
			CheckoutInfo checkoutInfoOne = checkoutService.prepareCheckout(itemOne);
			CheckoutInfo checkoutInfoTwo = checkoutService.prepareCheckout(itemTwo);
			
			orderTravelerSaveMethod(search, flightOne, dateOne, itemOne, returnTypeOne, couponCode, couponCode1, totalPayment1, loggedCustomer, googleLogin, paymentMethod, coupon, coupon1, checkoutInfoOne);
			orderTravelerSaveMethod(search, flightTwo, dateTwo, itemTwo, returnTypeTwo, couponCode, couponCode1, totalPayment2, loggedCustomer, googleLogin, paymentMethod, coupon, coupon1, checkoutInfoTwo);
			
			checkoutService.prepareCheckoutReturn(itemOne, itemTwo);
			
			return "redirect:/flight_return_order_" + search.getId() + "&" + flightOne_id + "&" + itemOne_id + "&" + flightTwo_id + "&" + itemTwo_id;
		} catch (Exception e) {
			return "redirect:/flight_return_order_" + searchId + "&" + flightOne_id + "&" + itemOne_id + "&" + flightTwo_id + "&" + itemTwo_id;
		}
	}

	private void orderTravelerSaveMethod(SearchHistory search, ProductDetail flight, Date date, CartItem item, String returnType, String couponCode, String couponCode1, String totalPayment, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, PaymentMethod paymentMethod, Coupon coupon, Coupon coupon1,
			CheckoutInfo checkoutInfo) { 
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
		String dateTime = dateFormat.format(date);
		
		String orderName = flight.getCityOne() + "-" + flight.getCityTwo() + ":(" + flight.getFlightNum() + ")" + dateTime + ":" + flight.getDepTime() + "-" + flight.getArrTime() 
		+ ":(" + search.getPassengerNum() + ")" + ":(" + returnType + ")";
		
		Order order = orderRepo.findByCartItemOrder(item.getId());
		
		List<TravellerDetail> travellerDetails = travellerRepo.findTravellerByCurtItemAndProductDetail(flight, item);

		cartService.updateCartItemOrdered(item);

		loginControl(couponCode, couponCode1, totalPayment, loggedCustomer, googleLogin, flight, search, item,
				paymentMethod, orderName, order, travellerDetails, coupon, coupon1, checkoutInfo);
		
	}
	
	@GetMapping("/flight_return_order_{search_id}&{flightOne_id}&{itemOne_id}&{flightTwo_id}&{itemTwo_id}")
	public String orderReturnPage(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flightOne_id") Integer flightOne_id, 
			@PathVariable(name = "itemOne_id") Integer itemOne_id, 
			@PathVariable(name = "flightTwo_id") Integer flightTwo_id, 
			@PathVariable(name = "itemTwo_id") Integer itemTwo_id,
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
		
		ProductDetail flight1 = flightRepo.findById(flightOne_id).get();
		CartItem item1 = cartRepo.findById(itemOne_id).get();
		ProductDetail flight2 = flightRepo.findById(flightTwo_id).get();
		CartItem item2 = cartRepo.findById(itemTwo_id).get();
		
		List<TravellerDetail> travelers1 = productService.findTraveller(flight1, item1);
		List<TravellerDetail> travelers2 = productService.findTraveller(flight2, item2);

		CheckoutInfo checkoutInfo = checkoutService.prepareCheckoutReturn(item1, item2);
		Order order1 = orderRepo.findByCartItemOrder(itemOne_id);
		Order order2 = orderRepo.findByCartItemOrder(itemTwo_id);
		
		if (!search_id.equals(null)) {
			SearchHistory search = searchRepo.findById(search_id).get();
			model.addAttribute("search", search);
		} else {
			SearchHistory search = searchRepo.findByCart_id(itemOne_id);
			model.addAttribute("search", search);
		}
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order1.getId() + "&"+ order2.getId();
		Integer intAmount = (int) (order1.getPrice() * 100) + (int) (order2.getPrice() * 100);
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
			ZaakpayApiRequestParameters processPayment = transaction.processPaymentReturn(orderString, amount);
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
		}
		
		Coupon coupon1 = couponService.findCouponByCode(order1.getCouponCode());
		Coupon coupon2 = couponService.findCouponByCode(order2.getCouponCode());
		
		savedOrderReturnId1 = order1.getId();
		savedOrderReturnId2 = order2.getId();
		
		Double orderPrice = order1.getPrice() + order2.getPrice();

		model.addAttribute("orderOne", order1);
		model.addAttribute("orderPrice", orderPrice);
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelersOne", travelers1);
		model.addAttribute("itemOne", item1);
		model.addAttribute("flightOne", flight1);
		model.addAttribute("search_id", search_id);
		model.addAttribute("itemOne_id", itemOne_id);
		model.addAttribute("coupon1", coupon1);
		model.addAttribute("orderTwo", order2);
		model.addAttribute("travelersTwo", travelers2);
		model.addAttribute("itemTwo", item2);
		model.addAttribute("flightTwo", flight2);
		model.addAttribute("itemTwo_id", itemTwo_id);
		model.addAttribute("coupon2", coupon2);
		
		return "order/return/flight_order";
	}
	
	////Wallet segment
	
	@PostMapping("/flight_wallet_check")
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
		
		return "redirect:/flight_wallet_response";
	}
	
	@PostMapping("/flight_wallet_return_check")
	public String walletPaymentReturn(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			@RequestParam(name = "search_id") Integer search_id) {
		
		String email; 
		Customer customer; 
		Order order1 = orderRepo.findById(savedOrderReturnId1).get();
		Order order2 = orderRepo.findById(savedOrderReturnId2).get();
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByEmail(email);
			Wallet wallet = walletPayOrderReturn(customer, order1, order2);
			if (wallet != null) {
				updatedOrderReturnId1 = order1.getId();
				updatedOrderReturnId2 = order2.getId();
			}
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = walletPayOrderReturn(customer, order1, order2);
			if (wallet != null) {
				updatedOrderReturnId1 = order1.getId(); 
				updatedOrderReturnId2 = order2.getId();
			}
		}
		
		searchReturn_id_inner = search_id;
		
		return "redirect:/flight_wallet_return_response";
	}
	
	private Wallet walletPayOrderReturn(Customer customer, Order order1, Order order2) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order1.getId() + "&"+ order2.getId();
		return walletService.updateWalletBalanceByOrderReturn(customer, order1, order2, orderString, "");
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

	private Wallet walletPayOrder(Customer customer, Order order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
		return walletService.updateWalletBalanceByOrder(customer, order, orderString, "");
	}

	private Wallet walletPayOrderCancel(Customer customer, Order order) {
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
		return walletService.cancelWalletBalanceByOrder(customer, order, orderString, "");
	}
	
	@GetMapping("/flight_wallet_response")
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

		methodSSR(productDetail);

		SearchHistory search = searchRepo.findById(search_id_inner).get();
		
		ticketDetails(order, productDetail, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search);
		
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
		
		return "wallet/response";
	}
	
	@GetMapping("/flight_wallet_return_response")
	public String showWalletPaymentReturn(@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) throws MalformedURLException, IOException {
		
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
		
		Order order1 = orderRepo.findById(updatedOrderReturnId1).get();
		Order order2 = orderRepo.findById(updatedOrderReturnId2).get();
		
		model.addAttribute("orderId1", order1.getId());
		model.addAttribute("orderId2", order2.getId());
		ProductDetail productDetail1 = order1.getProductDetail();
		ProductDetail productDetail2 = order2.getProductDetail();

		methodSSR(productDetail1);
		methodSSR(productDetail2);

		SearchHistory search = searchRepo.findById(searchReturn_id_inner).get();
		
		ticketDetails(order1, productDetail1, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search);
		ticketDetails(order2, productDetail2, productDetailsController.basefareTravelerAdultReturn, productDetailsController.taxTravelerAdultReturn, productDetailsController.basefareTravelerChildReturn, 
				productDetailsController.taxTravelerChildReturn, productDetailsController.basefareTravelerInfantReturn, productDetailsController.taxTravelerInfantReturn, search);

		// Departure ticket segment ............................*****.....................
		String pnr1 = productDetail1.getPnr();
		model.addAttribute("pnrBarcodeOne", pnr1);
		model.addAttribute("orderStatusOOne", order1.getOrderStatus());
		
		String cityOne1 = productDetail1.getCityOne();
		String cityTwo1 = productDetail1.getCityTwo();
		City city10 = cityRepo.getCityByCode(cityOne1);
		City city20 = cityRepo.getCityByCode(cityTwo1);
		model.addAttribute("cityOneOne", city10.getCityName());
		model.addAttribute("cityTwoOne", city20.getCityName());
		model.addAttribute("city1One", cityOne1);
		model.addAttribute("city2One", cityTwo1);
		
		Date dateDep1 = productDetail1.getDate();  
		DateFormat dateFormat1 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime = dateFormat1.format(dateDep1);
		model.addAttribute("flightDateTimeOne", flightDateTime);
		
		Date dateOrder1 = order1.getCreatedTime();  
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime = dateFormat2.format(dateOrder1);
		model.addAttribute("orderDateTimeOne", orderDateTime);

		String photoImagePath1 = "";
		Brand brand1 = brandRepo.getBrandByName(productDetail1.getBrand().toLowerCase());
		if (brand1.equals(null)) {
			photoImagePath1 = brand1.getPhotosImagePath();
		} else {
			photoImagePath1 = "#";
		}
		model.addAttribute("brandPathOne", ".." + photoImagePath1);
		model.addAttribute("brandNameOne", brand1.getName());
		model.addAttribute("passengerPhoneOne", order1.getPassengerNum());
		model.addAttribute("passengerEmailOne", order1.getContactEmail());

		Integer dateInt = productDetail1.getDuration()/60;
		if (dateInt >= 10) {
			model.addAttribute("dateIntOne", dateInt);
		} else {
			model.addAttribute("dateIntOne", "0" + dateInt);
		}
		Integer timeInt = productDetail1.getDuration()%60;
		if (timeInt >= 10) {
			model.addAttribute("timeIntOne", timeInt);
		} else {
			model.addAttribute("timeIntOne", "0" + timeInt);
		}
		
		List<TravellerDetail> travellerDetails1 = travellerRepo.findTravellerByProductDetailAndOrder(productDetail1, order1);
		model.addAttribute("travellerDetailsOne", travellerDetails1);
        model.addAttribute("amountOne", order1.getPrice());

		model.addAttribute("productDetailOne", productDetail1);
		model.addAttribute("originTerminalOne", productDetail1.getTerminalDep());
		model.addAttribute("destinationTerminalOne", productDetail1.getTerminalArr());
		model.addAttribute("baggageOne", productDetail1.getBaggage());
		model.addAttribute("cabinBaggageOne", productDetail1.getCabinBaggage());
		//.............................******........................................
		
		// Return ticket segment ............................*****.....................
		String pnr2 = productDetail2.getPnr();
		model.addAttribute("pnrBarcodeTwo", pnr2);
		model.addAttribute("orderStatusOTwo", order2.getOrderStatus());
		
		String cityOne2 = productDetail2.getCityOne();
		String cityTwo2 = productDetail2.getCityTwo();
		City city11 = cityRepo.getCityByCode(cityOne2);
		City city21 = cityRepo.getCityByCode(cityTwo2);
		model.addAttribute("cityOneTwo", city11.getCityName());
		model.addAttribute("cityTwoTwo", city21.getCityName());
		model.addAttribute("city1Two", cityOne2);
		model.addAttribute("city2Two", cityTwo2);
		
		Date dateDep2 = productDetail2.getDate();  
		DateFormat dateFormat11 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime2 = dateFormat11.format(dateDep2);
		model.addAttribute("flightDateTimeTwo", flightDateTime2);
		
		Date dateOrder2 = order2.getCreatedTime();  
		DateFormat dateFormat22 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime2 = dateFormat22.format(dateOrder2);
		model.addAttribute("orderDateTimeTwo", orderDateTime2);
		
		String photoImagePath2 = "";
		Brand brand2 = brandRepo.getBrandByName(productDetail2.getBrand().toLowerCase());
		if (brand2.equals(null)) {
			photoImagePath2 = brand2.getPhotosImagePath();
		} else {
			photoImagePath2 = "#";
		}
		model.addAttribute("brandPathTwo", ".." + photoImagePath2);
		model.addAttribute("brandNameTwo", brand2.getName());
		model.addAttribute("passengerPhoneTwo", order1.getPassengerNum());
		model.addAttribute("passengerEmailTwo", order1.getContactEmail());

		Integer dateInt2 = productDetail2.getDuration()/60;
		if (dateInt2 >= 10) {
			model.addAttribute("dateIntTwo", dateInt2);
		} else {
			model.addAttribute("dateIntTwo", "0" + dateInt2);
		}
		Integer timeInt2 = productDetail2.getDuration()%60;
		if (timeInt2 >= 10) {
			model.addAttribute("timeIntTwo", timeInt2);
		} else {
			model.addAttribute("timeIntTwo", "0" + timeInt2);
		}
		
		List<TravellerDetail> travellerDetails2 = travellerRepo.findTravellerByProductDetailAndOrder(productDetail2, order2);
		model.addAttribute("travellerDetailsTwo", travellerDetails2);
        model.addAttribute("amountTwo", order2.getPrice());

		model.addAttribute("paymentSuccess", OrderStatus.SUCCESSFULL);
		model.addAttribute("productDetailTwo", productDetail2);
		model.addAttribute("originTerminalTwo", productDetail2.getTerminalDep());
		model.addAttribute("destinationTerminalTwo", productDetail2.getTerminalArr());
		model.addAttribute("baggageTwo", productDetail2.getBaggage());
		model.addAttribute("cabinBaggageTwo", productDetail2.getCabinBaggage());
		//.............................******........................................
		
		Path flightUpPath = Paths.get("../pdf-images/flight-up.png");
		Path flightDownPath = Paths.get("../pdf-images/flight-down.png");
		Path demoTicketPath = Paths.get("../pdf-images/demo-ticket.png");
		Path thumbLogoPath = Paths.get("../pdf-images/thumb-logo.png");
		model.addAttribute("flightUpPath", flightUpPath);
		model.addAttribute("flightDownPath", flightDownPath);
		model.addAttribute("demoTicketPath", demoTicketPath);
		model.addAttribute("thumbLogoPath", thumbLogoPath);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		orderUpdateWallet(order1);
		orderUpdateWallet(order2);
		
		return "wallet/return/response";
	}
	
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/return/response",
			method = {RequestMethod.POST})
	public String zaakpayResponseReturn (HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin, 
			@RequestParam(name = "search_id") Integer search_id) throws Exception {
		//com.easygofly.entity.Transaction transactions = new com.easygofly.entity.Transaction();
		
		searchReturn_id_inner = search_id;
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
		String part2 = parts[1]; 
		String[] orders = part2.split("&");
		
		Integer convert1 = Integer.parseInt(orders[0]);
		Integer convert2 = Integer.parseInt(orders[1]);
		Order order1 = orderRepo.findById(convert1).get();
		Order order2 = orderRepo.findById(convert2).get();
		ProductDetail productDetail1 = order1.getProductDetail();
		ProductDetail productDetail2 = order2.getProductDetail();
		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			orderService.updateOrder(order1, OrderStatus.CANCELLED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			orderService.updateOrder(order1, OrderStatus.FAILED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			orderService.updateOrder(order1, OrderStatus.FAILED);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
			orderService.updateOrder(order1, OrderStatus.SUCCESSFULL);
		} else {
			if (productDetail1.getPnr().equals(null) || productDetail1.getPnr().equals("")) {
				orderService.updateOrder(order1, OrderStatus.PENDING);
			} else {
				orderService.updateOrder(order1, OrderStatus.SUCCESSFULL);
			}
			
			Integer totalSeatRemaining = Integer.parseInt(productDetail1.getTotalSeats()) - order1.getPassengerNum();
			orderService.updateTotalPassenger(order1, totalSeatRemaining);
		}
		
		///return

		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			orderService.updateOrder(order2, OrderStatus.CANCELLED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			orderService.updateOrder(order2, OrderStatus.FAILED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			orderService.updateOrder(order2, OrderStatus.FAILED);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
			orderService.updateOrder(order2, OrderStatus.SUCCESSFULL);
		} else {
			if (productDetail2.getPnr().equals(null) || productDetail2.getPnr().equals("")) {
				orderService.updateOrder(order2, OrderStatus.PENDING);
			} else {
				orderService.updateOrder(order2, OrderStatus.SUCCESSFULL);
			}
			
			Integer totalSeatRemaining = Integer.parseInt(productDetail2.getTotalSeats()) - order2.getPassengerNum();
			orderService.updateTotalPassenger(order2, totalSeatRemaining);
		}
		
		try {
			CartItem cartItem1 = cartRepo.findById(order1.getCartId()).get();
			CartItem cartItem2 = cartRepo.findById(order1.getCartId()).get();
			if (!cartItem1.equals(null)) {
				if (order1.getOrderStatus().equals(OrderStatus.CANCELLED) || order1.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order1.getOrderStatus().equals(OrderStatus.FAILED) 
						|| order1.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem1.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCurtItemAndProductDetail(productDetail1, cartItem1);
						for (TravellerDetail travellerDetail : travellerDetail2) {
							travellerDetail.setCartItem(null);
							travellerRepo.save(travellerDetail);
						}
							
						searchService.updateSearchHistoryCart(searchHistory, cartItem1);
						searchHistory.setCartItem(null);
						searchRepo.save(searchHistory);
					}
					cartItem1.setSearchHistory(null);
					cartRepo.save(cartItem1);
					
					cartService.deleteCartItem(cartItem1.getId());
				}
			}
			
			//return
			if (!cartItem2.equals(null)) {
				if (order2.getOrderStatus().equals(OrderStatus.CANCELLED) || order2.getOrderStatus().equals(OrderStatus.SUCCESSFULL) || order2.getOrderStatus().equals(OrderStatus.FAILED) 
						|| order2.getOrderStatus().equals(OrderStatus.PENDING )) {
					List<SearchHistory> search = cartItem2.getSearchHistory();
					for (SearchHistory searchHistory : search) {
						List<TravellerDetail> travellerDetail2 = travellerRepo.findTravellerByCurtItemAndProductDetail(productDetail2, cartItem2);
						for (TravellerDetail travellerDetail : travellerDetail2) {
							travellerDetail.setCartItem(null);
							travellerRepo.save(travellerDetail);
						}
							
						searchService.updateSearchHistoryCart(searchHistory, cartItem2);
						searchHistory.setCartItem(null);
						searchRepo.save(searchHistory);
					}
					cartItem2.setSearchHistory(null);
					cartRepo.save(cartItem2);
					
					cartService.deleteCartItem(cartItem2.getId());
				}
			}
		} catch (Exception e) {
			return "redirect:/zaakpay/return/response";
		}
		
		return "redirect:/zaakpay/return/response";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/return/response",
			method = {RequestMethod.GET})
	public String zaakpayResponseSeReturn (Model model, 
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
		String[] parts = orderParam.split("R");
		String part2 = parts[1]; 
		String[] orders = part2.split("&");
		
		Integer convert1 = Integer.parseInt(orders[0]);
		Integer convert2 = Integer.parseInt(orders[1]);
		Order order1 = orderRepo.findById(convert1).get();
		Order order2 = orderRepo.findById(convert2).get();
		
		model.addAttribute("orderId1", order1.getId());
		model.addAttribute("orderId2", order2.getId());
		
		ProductDetail productDetail1 = order1.getProductDetail();
		ProductDetail productDetail2 = order2.getProductDetail();
		
		methodSSR(productDetail1);
		methodSSR(productDetail2);

		SearchHistory search = searchRepo.findById(searchReturn_id_inner).get();
		
		ticketDetails(order1, productDetail1, productDetailsController.basefareTravelerAdult, productDetailsController.taxTravelerAdult, productDetailsController.basefareTravelerChild, 
				productDetailsController.taxTravelerChild, productDetailsController.basefareTravelerInfant, productDetailsController.taxTravelerInfant, search);
		ticketDetails(order2, productDetail2, productDetailsController.basefareTravelerAdultReturn, productDetailsController.taxTravelerAdultReturn, productDetailsController.basefareTravelerChildReturn, 
				productDetailsController.taxTravelerChildReturn, productDetailsController.basefareTravelerInfantReturn, productDetailsController.taxTravelerInfantReturn, search);

		// Departure ticket segment ............................*****.....................
		String pnr1 = productDetail1.getPnr();
		model.addAttribute("pnrBarcodeOne", pnr1);
		model.addAttribute("orderStatusOOne", order1.getOrderStatus());
		
		String cityOne1 = productDetail1.getCityOne();
		String cityTwo1 = productDetail1.getCityTwo();
		City city10 = cityRepo.getCityByCode(cityOne1);
		City city20 = cityRepo.getCityByCode(cityTwo1);
		model.addAttribute("cityOneOne", city10.getCityName());
		model.addAttribute("cityTwoOne", city20.getCityName());
		model.addAttribute("city1One", cityOne1);
		model.addAttribute("city2One", cityTwo1);
		
		Date dateDep1 = productDetail1.getDate();  
		DateFormat dateFormat1 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime = dateFormat1.format(dateDep1);
		model.addAttribute("flightDateTimeOne", flightDateTime);
		
		Date dateOrder1 = order1.getCreatedTime();  
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime = dateFormat2.format(dateOrder1);
		model.addAttribute("orderDateTimeOne", orderDateTime);

		String photoImagePath1 = "";
		Brand brand1 = brandRepo.getBrandByName(productDetail1.getBrand().toLowerCase());
		if (brand1.equals(null)) {
			photoImagePath1 = brand1.getPhotosImagePath();
		} else {
			photoImagePath1 = "#";
		}
		model.addAttribute("brandPathOne", ".." + photoImagePath1);
		model.addAttribute("brandNameOne", brand1.getName());
		model.addAttribute("passengerPhoneOne", order1.getPassengerNum());
		model.addAttribute("passengerEmailOne", order1.getContactEmail());

		Integer dateInt = productDetail1.getDuration()/60;
		if (dateInt >= 10) {
			model.addAttribute("dateIntOne", dateInt);
		} else {
			model.addAttribute("dateIntOne", "0" + dateInt);
		}
		Integer timeInt = productDetail1.getDuration()%60;
		if (timeInt >= 10) {
			model.addAttribute("timeIntOne", timeInt);
		} else {
			model.addAttribute("timeIntOne", "0" + timeInt);
		}
		
		List<TravellerDetail> travellerDetails1 = travellerRepo.findTravellerByProductDetailAndOrder(productDetail1, order1);
		model.addAttribute("travellerDetailsOne", travellerDetails1);
        model.addAttribute("amountOne", order1.getPrice());

		model.addAttribute("productDetailOne", productDetail1);
		model.addAttribute("originTerminalOne", productDetail1.getTerminalDep());
		model.addAttribute("destinationTerminalOne", productDetail1.getTerminalArr());
		model.addAttribute("baggageOne", productDetail1.getBaggage());
		model.addAttribute("cabinBaggageOne", productDetail1.getCabinBaggage());
		//.............................******........................................
		
		// Return ticket segment ............................*****.....................
		String pnr2 = productDetail2.getPnr();
		model.addAttribute("pnrBarcodeTwo", pnr2);
		model.addAttribute("orderStatusOTwo", order2.getOrderStatus());
		
		String cityOne2 = productDetail2.getCityOne();
		String cityTwo2 = productDetail2.getCityTwo();
		City city11 = cityRepo.getCityByCode(cityOne2);
		City city21 = cityRepo.getCityByCode(cityTwo2);
		model.addAttribute("cityOneTwo", city11.getCityName());
		model.addAttribute("cityTwoTwo", city21.getCityName());
		model.addAttribute("city1Two", cityOne2);
		model.addAttribute("city2Two", cityTwo2);
		
		Date dateDep2 = productDetail2.getDate();  
		DateFormat dateFormat11 = new SimpleDateFormat("E, dd-MM-yyyy");  
	    String flightDateTime2 = dateFormat11.format(dateDep2);
		model.addAttribute("flightDateTimeTwo", flightDateTime2);
		
		Date dateOrder2 = order2.getCreatedTime();  
		DateFormat dateFormat22 = new SimpleDateFormat("dd-MM-yyyy");  
	    String orderDateTime2 = dateFormat22.format(dateOrder2);
		model.addAttribute("orderDateTimeTwo", orderDateTime2);
		
		String photoImagePath2 = "";
		Brand brand2 = brandRepo.getBrandByName(productDetail2.getBrand().toLowerCase());
		if (brand2.equals(null)) {
			photoImagePath2 = brand2.getPhotosImagePath();
		} else {
			photoImagePath2 = "#";
		}
		model.addAttribute("brandPathTwo", ".." + photoImagePath2);
		model.addAttribute("brandNameTwo", brand2.getName());
		model.addAttribute("passengerPhoneTwo", order1.getPassengerNum());
		model.addAttribute("passengerEmailTwo", order1.getContactEmail());

		Integer dateInt2 = productDetail2.getDuration()/60;
		if (dateInt2 >= 10) {
			model.addAttribute("dateIntTwo", dateInt2);
		} else {
			model.addAttribute("dateIntTwo", "0" + dateInt2);
		}
		Integer timeInt2 = productDetail2.getDuration()%60;
		if (timeInt2 >= 10) {
			model.addAttribute("timeIntTwo", timeInt2);
		} else {
			model.addAttribute("timeIntTwo", "0" + timeInt2);
		}
		
		List<TravellerDetail> travellerDetails2 = travellerRepo.findTravellerByProductDetailAndOrder(productDetail2, order2);
		model.addAttribute("travellerDetailsTwo", travellerDetails2);
        model.addAttribute("amountTwo", order2.getPrice());

		model.addAttribute("paymentSuccess", OrderStatus.SUCCESSFULL);
		model.addAttribute("productDetailTwo", productDetail2);
		model.addAttribute("originTerminalTwo", productDetail2.getTerminalDep());
		model.addAttribute("destinationTerminalTwo", productDetail2.getTerminalArr());
		model.addAttribute("baggageTwo", productDetail2.getBaggage());
		model.addAttribute("cabinBaggageTwo", productDetail2.getCabinBaggage());
		//.............................******........................................
				

		Path flightUpPath = Paths.get("../pdf-images/flight-up.png");
		Path flightDownPath = Paths.get("../pdf-images/flight-down.png");
		Path demoTicketPath = Paths.get("../pdf-images/demo-ticket.png");
		Path thumbLogoPath = Paths.get("../pdf-images/thumb-logo.png");
		model.addAttribute("flightUpPath", flightUpPath);
		model.addAttribute("flightDownPath", flightDownPath);
		model.addAttribute("demoTicketPath", demoTicketPath);
		model.addAttribute("thumbLogoPath", thumbLogoPath);
		
		
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
		
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "zaakpay/response-twoway";
		
		
	}
	
	private void ticketDetails(Order order, ProductDetail productDetail, String basefareTravelerAdult, String taxTravelerAdult, String basefareTravelerChild, String taxTravelerChild, 
			 String basefareTravelerInfant, String taxTravelerInfant, SearchHistory searchId ) throws MalformedURLException, IOException {
		List<String> travelerDetailsArray = new ArrayList<String>();
		List<TravellerDetail> travelers = productService.findTravellerByOrderANDProductDetail(productDetail, order);
		
		if (!productDetail.getTraceId().equals("offline")) {
			
			String[] airlineNoArray = productDetail.getFlightNum().split("-");
		
			for (TravellerDetail travellerDetail : travelers) {
				BaggageOnline baggageOnline = travellerDetail.getBaggageOnline();
				MealsOnline mealsOnline = travellerDetail.getMealOnline();
				SeatsOnline seatsOnline = travellerDetail.getSeatsOnline();
				
    			Date getDOB = travellerDetail.getDob();
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
				} else if (travellerDetail.getPaxType().equals("2")) {
    				Integer fareInt = Integer.parseInt(basefareTravelerChild) / searchId.getChildNum();
    				Integer taxInt = Integer.parseInt(taxTravelerChild) / searchId.getAdultNum();
    				baseFare = "" + fareInt;
    				tax = "" + taxInt;
    				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
				} else {
    				Integer fareInt = Integer.parseInt(basefareTravelerInfant) / searchId.getInfantNum();
    				Integer taxInt = Integer.parseInt(taxTravelerInfant) / searchId.getAdultNum();
    				baseFare = "" + fareInt;
    				tax = "" + taxInt;
    				travelerService.updateBasefareTax(travellerDetail, baseFare, tax);
				}
    			
    			String details = "{\r\n"
    					+ "		\"Title\": \"" + travellerDetail.getSalutation() + "\",\r\n"
    					+ "		\"FirstName\": \"" + travellerDetail.getFirstName() + "\",\r\n"
    					+ "		\"LastName\": \"" + travellerDetail.getLastName() + "\",\r\n"
    					+ "		\"PaxType\": " + travellerDetail.getPaxType() + ",\r\n"
    					+ "		\"DateOfBirth\": \"" + getDOB + "T00:00:00\",\r\n"
    					+ "		\"Gender\": " + genNum + ",\r\n"
    					+ "		\"PassportNo\": \"\",\r\n"
    					+ "		\"PassportExpiry\": \"\",\r\n"
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
    					+ "		\"IsLeadPax\": true,\r\n"
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
        	try {
				JSONObject jsonObjTicketResponse = jsonObjTicket.getJSONObject("Response").getJSONObject("Response").getJSONObject("FlightItinerary");
				JSONArray jsonArraySegment = jsonObjTicketResponse.getJSONArray("Segments");
//        	JSONObject jsonObjectSegments = new JSONObject();
				
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
			} catch (JSONException json) {
				JSONObject jsonObjTicketResponseError = jsonObjTicket.getJSONObject("Response").getJSONObject("Error");
				
				hasErrorCode = Integer.parseInt(jsonObjTicketResponseError.get("ErrorCode").toString());
				hasErrorMsg = jsonObjTicketResponseError.get("ErrorMessage").toString();
				
			} 

		}
	}
}
