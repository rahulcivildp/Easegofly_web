package com.easygofly.site.wallet;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.easygofly.entity.Customer;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.RechargeHistoryStatus;
import com.easygofly.entity.Wallet;
import com.easygofly.site.customer.CustomerRepository;
import com.easygofly.site.customer.CustomerService;
import com.easygofly.site.flight.order.TransactionRepository;
import com.easygofly.site.flight.order.TransactionService;
import com.easygofly.site.security.EasegoflyPhoneCustomerDetails;
import com.easygofly.site.security.oauth.CustomerOAuth2User;
import com.easygofly.site.setting.PaymentSettingBag;
import com.easygofly.site.setting.SettingService;
import com.easygofly.site.zaakpay.ChecksumGenerator;
import com.easygofly.site.zaakpay.Transaction;
import com.easygofly.site.zaakpay.ZaakpayApiRequestParameters;

@Controller
public class WalletController {

	@Autowired CustomerService customerService;
	@Autowired CustomerRepository customerRepository;
	@Autowired WalletService walletService ;
	@Autowired TransactionService transactionService;
	@Autowired TransactionRepository transactionRepository;
	@Autowired TotalTransactionService totalTransactionService;
	@Autowired private SettingService settingService;
	
	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;
	
	@GetMapping("/wallet-home")
	public String walletHome(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			responseBalance(model, customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			model.addAttribute("customer", customer);
			responseBalance(model, customer);
		} 
		
		
		return "wallet/wallet-home";
	}
	
	@PostMapping("/wallet-recharge")
	public String rechargeWallet(@RequestParam(name = "recharge-amount") Integer rechargeAmount,
			@RequestParam(name = "wallet_id") Integer wallet_id,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin ) {
		
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			Integer paisaValue = rechargeAmount * 100;
			
			walletService.setTempValue(customer, paisaValue);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Integer paisaValue = rechargeAmount * 100;
			
			walletService.setTempValue(customer, paisaValue);
		} 
		
		return "redirect:/wallet-confirm";
	}
	
	@GetMapping("/wallet-confirm")
	public String rechargeWalletPage(
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin,
			Model model, HttpServletRequest request) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			Wallet wallet = customer.getWallet();
			Integer paisaValueTemp = wallet.getTempValue() / 100;
			model.addAttribute("temp_value", paisaValueTemp);
			zaakpayPayment(model, request, wallet);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			Integer paisaValueTemp = wallet.getTempValue() / 100;
			model.addAttribute("temp_value", paisaValueTemp);
			zaakpayPayment(model, request, wallet);
		} 
		
		return "wallet/wallet-confirm";
	}

	private void zaakpayPayment(Model model, HttpServletRequest request, Wallet wallet) {
    	PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();
		
		
		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");  
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);
		
		
		
		String orderString = "TRN" + strDate1 + "T" + strDate2 + "R"+ wallet.getId();
		//String orderString = "TRN";
		Integer intAmount = (int) (wallet.getTempValue());
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
			ZaakpayApiRequestParameters processPayment = transaction.processPaymentRecharge(orderString, amount, paymentSettingBag);
			
			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/recharge",
			method = {RequestMethod.POST})
	public String zaakpayResponse (HttpServletRequest request, HttpServletResponse response,
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		

		Customer customer = new Customer();
        
        if (loggedCustomer != null) {
        	customer = customerService.getByPhone(loggedCustomer.getUsername());
		} else if (googleLogin !=  null) {
        	customer = customerService.getByEmail(googleLogin.getEmail());
			
		} 
        
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
	     
		rechargeStatus(customer);
		
		return "redirect:/zaakpay/recharge";
	}

	private void rechargeStatus(Customer customer) {
		RechargeHistory rechargeHistory = walletService.createRechargeHistoryByZaakpay(customer, parameter[8], parameter[18]);
		if (parameter[12].contains("Customer cancelled transaction. Transaction has failed")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.CANCELLED);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
		} else if (parameter[12].contains("The transaction was completed successfully.") || parameter[12].contains("Transaction has been settled.")) {
			RechargeHistory rechargeHistory2 = walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);
			walletService.updateWalletValue(customer, rechargeHistory2);
		} 
	}
	
	@CrossOrigin(origins = {"https://easegofly.com/"})
	@RequestMapping(value = "/zaakpay/recharge",
			method = {RequestMethod.GET})
	public String zaakpayResponseSe (HttpServletRequest request, Model model, HttpServletResponse response, 
			@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, @AuthenticationPrincipal CustomerOAuth2User googleLogin) throws Exception {
		


		Customer customer = new Customer();
        
        if (loggedCustomer != null) {
        	customer = customerService.getByPhone(loggedCustomer.getUsername());
		} else if (googleLogin !=  null) {
        	customer = customerService.getByEmail(googleLogin.getEmail());
			
		} 
        
		double balance = responseBalance(model, customer);
		boolean isProcessed = true;
		System.out.println(parameter);
	    com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		double amountIntRech = Double.parseDouble(parameter[0]) / 100;
		
		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown") ) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Unfortunately the transaction has failed.Please try again.")) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("The transaction was completed successfully.") || parameter[12].equals("Transaction has been settled.")) {
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
		
		return "zaakpay/response-wallet";
		
		
	}
	
	
	// Test Method
	@GetMapping("/test_wallet_send_email")
	public void testSendEmail() throws UnsupportedEncodingException, MessagingException {
		Customer cux = customerRepository.findById(69).get();
		com.easygofly.entity.Transaction selfTrans = transactionRepository.findById(1).get();
		
		walletService.sendSuccessEmail(cux, selfTrans.getPgTransId(), "2" , "100", false);
	}
	
	
	// Private Methods


	private double responseBalance(Model model, Customer customer) {
		Wallet wallet = customer.getWallet();
		double INRbalance = wallet.getBalance() / 100;
		model.addAttribute("wallet", wallet);
		model.addAttribute("balance", INRbalance);
		
		return INRbalance;
	}
	
	@GetMapping("/show-history")
	public String showHistory(@AuthenticationPrincipal EasegoflyPhoneCustomerDetails loggedCustomer, 
			@AuthenticationPrincipal CustomerOAuth2User googleLogin, Model model) {
		String email; 
		Customer customer; 
		if (loggedCustomer != null) {
			email = loggedCustomer.getUsername();
			customer = customerService.getByPhone(email);
			Wallet wallet = customer.getWallet();
			List<RechargeHistory> rechargeHistories = walletService.listAllRechargeHistory(wallet, Sort.by("date").ascending());
			model.addAttribute("rechargeHistories", rechargeHistories);
			model.addAttribute("customer", customer);
			
		} else if (googleLogin != null) {
			email = googleLogin.getEmail();
			customer = customerService.getByEmail(email);
			Wallet wallet = customer.getWallet();
			List<RechargeHistory> rechargeHistories = walletService.listAllRechargeHistory(wallet, Sort.by("date").ascending());

			model.addAttribute("rechargeStatus", RechargeHistoryStatus.SUCCESSFULL);
			model.addAttribute("rechargeHistories", rechargeHistories);
			model.addAttribute("customer", customer);
		}
		return "wallet/show-history";
	}
}
