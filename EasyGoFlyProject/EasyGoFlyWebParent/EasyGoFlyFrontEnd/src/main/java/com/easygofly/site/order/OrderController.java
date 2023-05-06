package com.easygofly.site.order;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.ccavenue.security.AesCryptUtil;
import com.easygofly.entity.CartItem;
import com.easygofly.entity.Customer;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.PaymentMethod;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.SearchHistory;
import com.easygofly.entity.Setting;
import com.easygofly.entity.TravellerDetail;
import com.easygofly.entity.User;
import com.easygofly.site.checkout.CheckoutInfo;
import com.easygofly.site.checkout.CheckoutService;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.FlightRepository;
import com.easygofly.site.flight.ProductDetailService;
import com.easygofly.site.search.SearchHistoryRepository;
import com.easygofly.site.security.EasyGoFlyCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.shoppingCart.CartItemRepository;
import com.easygofly.site.shoppingCart.CartItemService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Config;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class OrderController {
	
	@Autowired private CustomerService customerService;
	@Autowired private FlightRepository flightRepo;
	@Autowired private SearchHistoryRepository searchRepo;
	@Autowired private CartItemRepository cartRepo;
	@Autowired private CartItemService cartService;
	@Autowired private OrderService orderService;
	@Autowired private OrderRepository orderRepo;
	@Autowired private CheckoutService checkoutService;
	@Autowired private ProductDetailService productService;
	@Autowired private SettingService settingService;
	
	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;
	
	@PostMapping("/flight_order_save")
	public String createNewOrder(@RequestParam(name = "search_id") Integer searchId, 
			@RequestParam(name = "flight_id") Integer flightId,
			@RequestParam(name = "item_id") Integer item_id,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			HttpServletRequest request) {
		try {
			

			
			ProductDetail flight = flightRepo.findById(flightId).get();
			SearchHistory search = searchRepo.findById(searchId).get();
			CartItem item = cartRepo.findById(item_id).get();
			
			String paymentType = "PAYMENT_GATEWAY";
			PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);
			
			CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
			
			Date date = flight.getDate();  
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
		    String dateTime = dateFormat.format(date);
			
			String orderName = flight.getCityOne() + "-" + flight.getCityTwo() + ":(" + flight.getFlightNum() + ")" + dateTime + ":" + flight.getDepTime() + "-" + flight.getArrTime() + ":(" + search.getPassengerNum() + ")";

			Order order = orderRepo.findByCartItemOrder(item_id);
			
			cartService.updateCartItemOrdered(item);
			
			String email; 
			Customer customer; 
			if (loggedCustomer != null) {
				email = loggedCustomer.getUsername();
				customer = customerService.getByEmail(email);
				saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer);
				
			} else if (googleLogin != null) {
				email = googleLogin.getEmail();
				customer = customerService.getByEmail(email);
				saveOrderCreate(flight, search, item, paymentMethod, checkoutInfo, orderName, order, customer);
			}
			
			return "redirect:/flight_order_" + search.getId() + "&" + flightId + "&" + item_id;
		} catch (Exception e) {
			return "redirect:/flight_order_" + searchId + "&" + flightId + "&" + item_id;
		}
		
	}

	private void saveOrderCreate(ProductDetail flight, SearchHistory search, CartItem item, PaymentMethod paymentMethod,
			CheckoutInfo checkoutInfo, String orderName, Order order, Customer customer) {
		if (order == null) {
			orderService.createOrder(customer, item, flight, paymentMethod, checkoutInfo, search, orderName);
		}else if (item.getId() != order.getCartId()) {
			orderService.createOrder(customer, item, flight, paymentMethod, checkoutInfo, search, orderName);
		}
	}
	
	@GetMapping("/flight_order_{search_id}&{flight_id}&{item_id}")
	public String orderPage(@PathVariable(name = "search_id") Integer search_id, 
			@PathVariable(name = "flight_id") Integer flight_id,
			@PathVariable(name = "item_id") Integer item_id, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, HttpServletRequest request) throws UnsupportedEncodingException {
		
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
		
		ProductDetail flight = flightRepo.findById(flight_id).get();
		SearchHistory search = searchRepo.findById(search_id).get();
		CartItem item = cartRepo.findById(item_id).get();
		
		List<TravellerDetail> travelers = productService.findTraveller(flight, item);

		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(item);
		Order order = orderRepo.findByCartItemOrder(item_id);
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
	    DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
	    DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
	    String strDate1 = dateFormat1.format(date);
	    String strDate2 = dateFormat2.format(date);
		
		String orderString = "EGF" + strDate1 + "T" + strDate2 + "R"+ order.getId();
		Integer intAmount = (int) (checkoutInfo.getPaymentTotal() * 100);
		//String amount = "" + intAmount;
		String amount = "100";

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/* ------ CCAVENUE -------- */ /*
		String accessCode= "AVJC30KC30BM66CJMB";		//Put in the Access Code in quotes provided by CCAVENUES.
		String workingKey = "E99274310106A9AF3DB4AF4D3073863E";    //Put in the 32 Bit Working Key provided by CCAVENUES.  
		Enumeration enumeration=request.getParameterNames();
		String ccaRequest="", pname="", pvalue="";
		while(enumeration.hasMoreElements()) {
		     pname = ""+enumeration.nextElement();
		     pvalue = request.getParameter(pname);
		     ccaRequest = ccaRequest + pname + "=" + URLEncoder.encode(pvalue,"UTF-8") + "&";
		}
		AesCryptUtil aesUtil=new AesCryptUtil(workingKey);
		String encRequest = aesUtil.encrypt(ccaRequest);
		model.addAttribute("encRequest", encRequest);
		model.addAttribute("accessCode", accessCode);
		*/
		
		model.addAttribute("order_id", order.getId());
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("travelers", travelers);
		model.addAttribute("item", item);
		model.addAttribute("search", search);
		model.addAttribute("flight", flight);
		
		return "order/flight_order";
	}
	
	/**/
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/response",
			method = {RequestMethod.POST})
	public String zaakpayResponse (HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		com.easygofly.entity.Transaction transactions = new com.easygofly.entity.Transaction();
		
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
	    for (String string : parameter) {
	        System.out.println("Array Parameters: " + string);
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
		
		if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			orderService.updateOrder(order, OrderStatus.CANCELLED);
		} else if (parameter[12].equals("The transaction was completed successfully.")) {
			orderService.updateOrder(order, OrderStatus.SUCCESSFULL);
		}
	    
		return "redirect:/zaakpay/response";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/response",
			method = {RequestMethod.GET})
	public String zaakpayResponseSe (HttpServletRequest request, Model model, HttpServletResponse response, 
			@AuthenticationPrincipal EasyGoFlyCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		
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

		//Order order = orderRepo.findByCartItemOrder(item_id); 
		String orderParam = parameter[8];
		model.addAttribute("orderId", orderParam);
		String[] parts = orderParam.split("R");
		String part2 = parts[1]; // 034556
		Integer convert = Integer.parseInt(part2);
		Order order = orderRepo.findById(convert).get();
		ProductDetail productDetail = order.getProductDetail();
		Product product = productDetail.getProduct();
		User user = product.getUser();
		model.addAttribute("user", user);
		//CartItem cartItem = cartRepo.findById(order.getCartId()).get();
		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown") ) {
			model.addAttribute("paymentCancelled", parameter[12]);
		}
		
		List<Setting> settings = settingService.getGeneralSettingBag();
		
		
		Double amount = Double.parseDouble(parameter[0])/100;
		
		model.addAttribute("paymentSuccess", parameter[12]);
		model.addAttribute("amount", amount);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "zaakpay/response";
		
		
	}

	@GetMapping("/newpage")
	public String hello(@RequestParam(value = "name", defaultValue = "World",
    required = true) String name, Model model) {
    model.addAttribute("name", name);
    return "hello";
	}
	/*
	@PostMapping("/payg")
	public void payg(HttpServletRequest request, HttpServletResponse response) throws IOException {
		com.easygofly.site.payg.Order ordr = new com.easygofly.site.payg.Order();
		Process process = ordr.create(request);
		
		InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line,output="";
        while ((line = br.readLine()) != null) {
        	output += line;
        }
      
        System.out.println(request.toString());
        
		response.getWriter().write(output);
	}*/
	
	/*
	@PostMapping("/phonepe")
	public void phonepe() throws NoSuchAlgorithmException, IOException, InterruptedException {
		Phonepe phpe = new Phonepe();
		phpe.authentication();
	}
	*/
}
