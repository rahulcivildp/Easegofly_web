package com.easygofly.site.apipayment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.easygofly.entity.Customer;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.RechargeHistoryStatus;
import com.easygofly.entity.RideOrder;
import com.easygofly.entity.Wallet;
import com.easygofly.site.customer.CustomerRepository;
import com.easygofly.site.flight.order.TransactionRepository;
import com.easygofly.site.flight.order.TransactionService;
import com.easygofly.site.setting.PaymentSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.wallet.TotalTransactionService;
import com.easygofly.site.wallet.WalletController;
import com.easygofly.site.wallet.WalletService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class ApiPaymentController {
	@Autowired CustomerRepository customerRepository;
	@Autowired WalletService walletService ;
	@Autowired private SettingService settingService;
	@Autowired WalletController walletController;
	@Autowired TransactionService transactionService;
	@Autowired TransactionRepository transactionRepository;
	@Autowired TotalTransactionService totalTransactionService;
	@Autowired private RideOrderRepository rideOrderRepo ;
	@Autowired private PaymentService paymentService;

	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;
	
	// Wallet Paymet API
	
	@GetMapping("/wallet-recharge/api_{recharge}_{cust_id}")
	public String rechargeWalletAPI(@PathVariable("recharge") Integer rechargeAmount,
			@PathVariable("cust_id") Integer cust_id ) {
		
		System.out.println(rechargeAmount);
		System.out.println(cust_id);
		
		Customer customer = customerRepository.findById(cust_id).get(); 
		if (customer != null) {
			Integer paisaValue = rechargeAmount * 100;
			walletService.setTempValue(customer, paisaValue);
		} 
		
		return "redirect:/wallet-confirm/api_" + cust_id;
	}
	
	@GetMapping("/wallet-confirm/api_{user}")
	public String rechargeWalletPageAPI(@PathVariable("user") Integer user,
			Model model, HttpServletRequest request) {
		Customer customer = customerRepository.findById(user).get(); ; 
		if (customer != null) {
			Wallet wallet = customer.getWallet();
			Integer paisaValueTemp = wallet.getTempValue() / 100;
			model.addAttribute("temp_value", paisaValueTemp);
			zaakpayPaymentAPI(model, request, customer);
		}
		
		return "wallet/wallet-confirm-api";
	}

	private void zaakpayPaymentAPI(Model model, HttpServletRequest request, Customer customer) {
    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		
		
		String orderString = "TRN" + strDate1 + "T" + strDate2 + "CUST"+ customer.getId();
		//String orderString = "TRN";
		Integer intAmount = (int) (customer.getWallet().getTempValue());
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
			ZaakpayApiRequestParameters processPayment = transaction.processPaymentRechargeAPI(orderString, amount, paymentSettingBag, customer.getEmail());
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/recharge/api", method = {RequestMethod.POST})
	public String zaakpayResponseAPI (HttpServletRequest request, HttpServletResponse response) throws Exception {
		
        
    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		
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
	    
	    Boolean verifyChecksum = checksumGenerator.verifyChecksum(paymentSettingBag.getSecretKey(),checksumString,request.getParameter("checksum")) ;
	    verifiedChecksum = verifyChecksum;
	    checksum = request.getParameter("checksum");
	    responseParameters = transaction.getResponseParameters();
		
		return "redirect:/zaakpay/recharge/api";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/recharge/api",
			method = {RequestMethod.GET})
	public String zaakpayResponseSeAPI (HttpServletRequest request, Model model, HttpServletResponse response) throws Exception {

	    String[] orderId = parameter[8].split("CUST");
		Customer customer = customerRepository.findById(Integer.parseInt(orderId[1])).get();
        
		double balance = walletController.responseBalance(model, customer);
		boolean isProcessed = false;
		System.out.println(parameter);
	    com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		double amountIntRech = Double.parseDouble(parameter[0]) / 100;
		
		RechargeHistory rechargeHistory = walletService.createRechargeHistoryByZaakpay(customer, parameter[8], parameter[18]);
		
		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown") ) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
			RechargeHistory rechargeHistory2 = walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);
			walletService.updateWalletValue(customer, rechargeHistory2);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
		}

	    totalTransactionService.createTotalTransaction(customer, Double.parseDouble(selfTrans.getAmount()), false, true, null, null, null, selfTrans.getId(), OrderStatus.NEW);
		walletService.sendSuccessEmail(customer, selfTrans.getPgTransId(), "" + amountIntRech, "" + balance, isProcessed);
	    
		System.out.println(parameter);

		model.addAttribute("customer", customer);
		model.addAttribute("amountRecharged", amountIntRech);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "zaakpay/response-wallet-api";
		
	}
	

	// Cab Order Paymet API

	
	@GetMapping("/cab/order/api_{user}_{order}")
	public String cabOrderAPI(@PathVariable("user") Integer user,
			@PathVariable("order") Integer order,
			Model model, HttpServletRequest request) {
		System.out.println(user + ".........................................................................");
		Customer customer = customerRepository.findById(user).get();
		RideOrder rideOrder = rideOrderRepo.findById(order).get();
		System.out.println(user + ".........................................................................");
		
		if (customer != null) {
			Double paisaValue = rideOrder.getTotalAmount() * 100;
			Integer paisaValueTemp =  paisaValue.intValue();
			model.addAttribute("temp_value", rideOrder.getTotalAmount());
			zaakpayPaymentAPICabOrder(model, request, rideOrder.getOrderName(), paisaValueTemp, customer.getEmail());
		}
		
		return "cab/order/caborder-confirm-api";
	}
	

	@GetMapping("/cab/order/api_new_")
	public String cabOrderAPI(
			Model model) {

		
		return "cab/order/caborder-confirm-api";
	}

	private void zaakpayPaymentAPICabOrder(Model model, HttpServletRequest request, String orderString, Integer amountInt, String email) {
    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		
		/* ------ ZAAKPAY -------- */ /**/
		
		String amount = "" + amountInt;
		
		for (Cookie cookie : request.getCookies()) {
			if(cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}
		
		Transaction transaction = new Transaction();
		System.out.println(orderString + ".........................................................................");
		System.out.println(amount);
		System.out.println(email);
		
		try {
			ZaakpayApiRequestParameters processPayment = transaction.processCabOrderPaymentAPI(orderString, amount, paymentSettingBag, email);
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/cab/response/api", method = {RequestMethod.POST})
	public String zaakpayResponseCabOrderAPI (HttpServletRequest request, HttpServletResponse response) throws Exception {
		
        
    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		
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
	    
	    Boolean verifyChecksum = checksumGenerator.verifyChecksum(paymentSettingBag.getSecretKey(),checksumString,request.getParameter("checksum")) ;
	    verifiedChecksum = verifyChecksum;
	    checksum = request.getParameter("checksum");
	    responseParameters = transaction.getResponseParameters();
		
		return "redirect:/zaakpay/cab/response/api";
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/cab/response/api",
			method = {RequestMethod.GET})
	public String zaakpayResponseSeCabOrderAPI (HttpServletRequest request, Model model, HttpServletResponse response) throws Exception {

	    String[] ids = parameter[8].split("ID");
		Customer customer = customerRepository.findById(Integer.parseInt(ids[1])).get();
		RideOrder rideOrder = rideOrderRepo.findById(Integer.parseInt(ids[2])).get();
		List<RideOrder> rideOrders = new ArrayList<>();
		rideOrders.add(rideOrder);
        
		boolean isProcessed = false;
		System.out.println(parameter);
	    com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		double amountIntRech = Double.parseDouble(parameter[0]) / 100;
		
		
		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown") ) {
		    rideOrder.setStatus(OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
		    rideOrder.setStatus(OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
		    rideOrder.setStatus(OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Customer cancelled transaction. Transaction has failed")) {
		    rideOrder.setStatus(OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
		    rideOrder.setStatus(OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
		    rideOrder.setStatus(OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
		    rideOrder.setStatus(OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
		    rideOrder.setStatus(OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
		    totalTransactionService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), false, true, rideOrders, selfTrans.getId(), OrderStatus.SUCCESSFULL);
		    rideOrder.setStatus(OrderStatus.SUCCESSFULL);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
		}

	    rideOrderRepo.save(rideOrder);
	    paymentService.sendSuccessEmail(customer, selfTrans.getPgTransId(), isProcessed, rideOrder);
	    
		System.out.println(parameter);

		model.addAttribute("customer", customer);
		model.addAttribute("amountRecharged", amountIntRech);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);
		
		return "cab/order/response-cab-order-api";
		
	}
	
}
