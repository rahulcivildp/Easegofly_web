package com.easygofly.site.apipayment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.easygofly.entity.BusOrder;
import com.easygofly.entity.Customer;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.RechargeHistoryStatus;
import com.easygofly.entity.RideOrder;
import com.easygofly.entity.TotalTransaction;
import com.easygofly.site.bus.BusOrderRepository;
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
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private WalletService walletService;
	@Autowired
	private SettingService settingService;
	@Autowired
	private WalletController walletController;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private TotalTransactionService totalTransactionService;
	@Autowired
	private RideOrderRepository rideOrderRepo;
	@Autowired
	private PaymentService paymentService;

	@Autowired private BusOrderRepository busOrderRepo;

	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;

	// Wallet Paymet API

	@GetMapping("/wallet-recharge/api_{recharge}_{cust_id}")
	public String rechargeWalletAPI(@PathVariable("recharge") Integer rechargeAmount,
			@PathVariable("cust_id") Integer cust_id, HttpServletRequest request, Model model) {

		System.out.println(rechargeAmount);
		System.out.println(cust_id);

		Customer customer = customerRepository.findById(cust_id).get();
		if (customer != null) {
			Integer paisaValue = rechargeAmount * 100;
			walletService.setTempValue(customer, paisaValue);
		}


		return "redirect:/wallet-confirm/api_" + rechargeAmount + "_" + cust_id;
	}
	
	@GetMapping("/wallet-confirm/api_{recharge}_{cust_id}")
	public String rechargeWalletPageAPI(@PathVariable("recharge") Integer rechargeAmount,
			@PathVariable("cust_id") Integer cust_id, HttpServletRequest request, Model model) {

		System.out.println(rechargeAmount);
		System.out.println(cust_id);

		Customer customer = customerRepository.findById(cust_id).get();

		zaakpayPaymentAPI(request, customer, model);
		
		return "wallet/wallet-confirm-api";
	}
	
	private void zaakpayPaymentAPI(HttpServletRequest request, Customer customer, Model model) {
		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);

		String orderString = "TRN" + strDate1 + "T" + strDate2 + "CUST" + customer.getId();
		// String orderString = "TRN";
		Integer intAmount = (int) (customer.getWallet().getTempValue());
		String amount = "" + intAmount;
		// String amount = "100";

		// Cookie cookie = request.getCookies().get("JSESSIONID");
		// String value = cookie.getValue();

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}

		Transaction transaction = new Transaction();

		try {
			ZaakpayApiRequestParameters processPayment = transaction.processPaymentRechargeAPI(orderString, amount,
					paymentSettingBag, customer.getEmail());


			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/recharge/api", method = { RequestMethod.POST })
	public String zaakpayResponseAPI(HttpServletRequest request, HttpServletResponse response) throws Exception {

		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		Transaction transaction = new Transaction();
		ChecksumGenerator checksumGenerator = new ChecksumGenerator();
		String checksumString = "";
		Integer n = 0;
		for (String param : transaction.getResponseParameters()) {
			checksumString = checksumString + param + "=" + request.getParameter(param);
			checksumString = checksumString + "&";
			// This will create the checksum string against every parameter.
			parameter[n] = request.getParameter(param);
			n += 1;
		}
		for (String string : parameter) {
			System.out.println("Array Parameters: " + string);
		}

		Boolean verifyChecksum = checksumGenerator.verifyChecksum(paymentSettingBag.getSecretKey(), checksumString,
				request.getParameter("checksum"));
		verifiedChecksum = verifyChecksum;
		checksum = request.getParameter("checksum");
		responseParameters = transaction.getResponseParameters();

		return "redirect:/zaakpay/recharge/api";
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/recharge/api", method = { RequestMethod.GET })
	public String zaakpayResponseSeAPI(HttpServletRequest request, Model model, HttpServletResponse response)
			throws Exception {

		String[] orderId = parameter[8].split("CUST");
		Customer customer = customerRepository.findById(Integer.parseInt(orderId[1])).get();

		boolean isProcessed = false;
		System.out.println(Arrays.toString(parameter));
		System.out.println("Param Outer : " + parameter[12]);
		com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		double amountIntRech = Double.parseDouble(parameter[0]) / 100;

		RechargeHistory rechargeHistory = walletService.createRechargeHistoryByZaakpay(customer, parameter[8],
				parameter[18]);

		TotalTransaction trn = totalTransactionService.createTotalTransaction(customer, Double.parseDouble(selfTrans.getAmount()), false, true,
				null, null, null, selfTrans.getId(), OrderStatus.NEW);
		

		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.CANCELLED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Customer cancelled transaction. Transaction has failed")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.CANCELLED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains(
				"Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			walletService.updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("100")) {
			if (parameter[12].contains("The transaction was completed successfully.")
					|| parameter[12].contains("Transaction has been settled.")) {
				RechargeHistory rechargeHistory2 = walletService.updateRechargeHistoryStatus(rechargeHistory,
						RechargeHistoryStatus.SUCCESSFULL);
				walletService.updateWalletValue(customer, rechargeHistory2);
				totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.SUCCESSFULL);
				isProcessed = true;
				model.addAttribute("paymentSuccess", parameter[12]);
			}
		} else if (parameter[12].contains("The transaction was completed successfully.")
				|| parameter[12].contains("Transaction has been settled.")) {
			RechargeHistory rechargeHistory2 = walletService.updateRechargeHistoryStatus(rechargeHistory,
					RechargeHistoryStatus.SUCCESSFULL);
			walletService.updateWalletValue(customer, rechargeHistory2);
			totalTransactionService.updateTotalTransactionStatus(trn, OrderStatus.SUCCESSFULL);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
			double balance = walletController.responseBalance(model, customer);
			walletService.sendSuccessEmail(customer, selfTrans.getPgTransId(), "" + amountIntRech, "" + balance,
					isProcessed);
		}

		model.addAttribute("customer", customer);
		model.addAttribute("amountRecharged", amountIntRech);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);

		return "zaakpay/response-wallet-api";

	}

	// Cab Order Paymet API

	@GetMapping("/cab/order/api_{user}_{order}")
	public String cabOrderAPI(@PathVariable("user") Integer user, @PathVariable("order") Integer order, HttpServletRequest request) {
		Customer customer = customerRepository.findById(user).get();
		RideOrder rideOrder = rideOrderRepo.findById(order).get();
		
		System.out.println(customer.getId());
		System.out.println(rideOrder.getId());

		return "redirect:/cab/order/api-confirm_" + user + "_" + order;
	}

	@GetMapping("/cab/order/api-confirm_{user}_{order}")
	public String cabOrderRedirectAPI(@PathVariable("user") Integer user, @PathVariable("order") Integer order, HttpServletRequest request, Model model) {

		Customer customer = customerRepository.findById(user).get();
		RideOrder rideOrder = rideOrderRepo.findById(order).get();
		
		System.out.println(user);
		System.out.println(order);


		if (customer != null) {
			Double paisaValue = rideOrder.getTotalAmount() * 100;
			Integer paisaValueTemp = paisaValue.intValue();
			zaakpayPaymentAPICabOrder(model, request, paisaValueTemp, rideOrder, customer);
			model.addAttribute("temp_value", rideOrder.getTotalAmount());
		}

		return "cab/order/caborder-confirm-api";
	}

	private void zaakpayPaymentAPICabOrder(Model model, HttpServletRequest request,
			Integer amountInt, RideOrder rideOrder, Customer customer) {
		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		/* ------ ZAAKPAY -------- */ /**/
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(currentDate);
		rideOrder.setOrderName("EGF" + formattedDate + "ID" + rideOrder.getId() + "ID" + customer.getId());
		rideOrderRepo.save(rideOrder);

		String orderString = rideOrder.getOrderName();

		String amount = "" + amountInt;

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}

		Transaction transaction = new Transaction();
		System.out.println(orderString + ".........................................................................");
		System.out.println(amount);
		System.out.println(customer.getEmail());

		try {
			ZaakpayApiRequestParameters processPayment = transaction.processCabOrderPaymentAPI(orderString, amount,
					paymentSettingBag, customer.getEmail());

			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/cab/response/api", method = { RequestMethod.POST })
	public String zaakpayResponseCabOrderAPI(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		Transaction transaction = new Transaction();
		ChecksumGenerator checksumGenerator = new ChecksumGenerator();
		String checksumString = "";
		Integer n = 0;
		for (String param : transaction.getResponseParameters()) {
			checksumString = checksumString + param + "=" + request.getParameter(param);
			checksumString = checksumString + "&";
			// This will create the checksum string against every parameter.
			parameter[n] = request.getParameter(param);
			n += 1;
		}
		for (String string : parameter) {
			System.out.println("Array Parameters: " + string);
		}

		Boolean verifyChecksum = checksumGenerator.verifyChecksum(paymentSettingBag.getSecretKey(), checksumString,
				request.getParameter("checksum"));
		verifiedChecksum = verifyChecksum;
		checksum = request.getParameter("checksum");
		responseParameters = transaction.getResponseParameters();

		return "redirect:/zaakpay/cab/response/api";
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/cab/response/api", method = { RequestMethod.GET })
	public String zaakpayResponseSeCabOrderAPI(HttpServletRequest request, Model model, HttpServletResponse response)
			throws Exception {

		String[] ids = parameter[8].split("ID");
		Customer customer = customerRepository.findById(Integer.parseInt(ids[2])).get();
		RideOrder rideOrder = rideOrderRepo.findById(Integer.parseInt(ids[1])).get();
		List<RideOrder> rideOrders = new ArrayList<>();
		rideOrders.add(rideOrder);

		boolean isProcessed = false;
		System.out.println(parameter);
		com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		double amountIntRech = Double.parseDouble(parameter[0]) / 100;

		if (parameter[9].contains("Not Found") && parameter[10].equals("unknown")) {
			updateRide(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			updateRide(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			updateRide(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Customer cancelled transaction. Transaction has failed")) {
			updateRide(rideOrder, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			updateRide(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			updateRide(rideOrder, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains(
				"Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			updateRide(rideOrder, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			updateRide(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("100")) {
			if (parameter[12].contains("The transaction was completed successfully.")
				|| parameter[12].contains("Transaction has been settled.")) {
			totalTransactionService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), false, true,
					rideOrders, selfTrans.getId(), OrderStatus.SUCCESSFULL);
			updateRide(rideOrder, OrderStatus.SUCCESSFULL);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
			paymentService.sendSuccessEmail(customer, selfTrans.getPgTransId(), isProcessed, rideOrder);
		}
		} else if (parameter[12].contains("The transaction was completed successfully.")
				|| parameter[12].contains("Transaction has been settled.")) {
			totalTransactionService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), false, true,
					rideOrders, selfTrans.getId(), OrderStatus.SUCCESSFULL);
			updateRide(rideOrder, OrderStatus.SUCCESSFULL);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
			paymentService.sendSuccessEmail(customer, selfTrans.getPgTransId(), isProcessed, rideOrder);
		}

		System.out.println(parameter);

		model.addAttribute("customer", customer);
		model.addAttribute("amountRecharged", amountIntRech);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);

		return "cab/order/response-cab-order-api";

	}

	// Bus Order Paymet API

	@GetMapping("/bus/order/api_{user}_{order}")
	public String busOrderAPI(@PathVariable("user") Integer user, @PathVariable("order") Integer order, HttpServletRequest request) {
		Customer customer = customerRepository.findById(user).get();
		BusOrder rideOrder = busOrderRepo.findById(order).get();
		
		System.out.println(customer.getId());
		System.out.println(rideOrder.getId());

		return "redirect:/bus/order/api-confirm_" + user + "_" + order;
	}

	@GetMapping("/bus/order/api-confirm_{user}_{order}")
	public String busOrderRedirectAPI(@PathVariable("user") Integer user, @PathVariable("order") Integer order, HttpServletRequest request, Model model) {

		Customer customer = customerRepository.findById(user).get();
		BusOrder rideOrder = busOrderRepo.findById(order).get();
		
		System.out.println(user);
		System.out.println(order);


		if (customer != null) {
			Double paisaValue = rideOrder.getPrice() * 100;
			Integer paisaValueTemp = paisaValue.intValue();
			zaakpayPaymentAPIbusOrder(model, request, paisaValueTemp, rideOrder, customer);
			model.addAttribute("temp_value", rideOrder.getPrice());
		}

		return "bus/order/busorder-confirm-api";
	}

	private void zaakpayPaymentAPIbusOrder(Model model, HttpServletRequest request,
			Integer amountInt, BusOrder rideOrder, Customer customer) {
		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		/* ------ ZAAKPAY -------- */ /**/
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(currentDate);
		rideOrder.setName("EGF" + formattedDate + "ID" + rideOrder.getId() + "ID" + customer.getId());
		busOrderRepo.save(rideOrder);

		String orderString = rideOrder.getName();

		String amount = "" + amountInt;

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				model.addAttribute("JSESSIONID", value);
			}
		}

		Transaction transaction = new Transaction();
		System.out.println(orderString + ".........................................................................");
		System.out.println(amount);
		System.out.println(customer.getEmail());

		try {
			ZaakpayApiRequestParameters processPayment = transaction.processBusOrderPaymentAPI(orderString, amount,
					paymentSettingBag, customer.getEmail());

			model.addAttribute("entrySet", processPayment.getRequestParameters().entrySet());
			model.addAttribute("requestUrl", processPayment.getRequestUrl());
			model.addAttribute("checksum", processPayment.getChecksum());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/bus/response/api", method = { RequestMethod.POST })
	public String zaakpayResponsebusOrderAPI(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		Transaction transaction = new Transaction();
		ChecksumGenerator checksumGenerator = new ChecksumGenerator();
		String checksumString = "";
		Integer n = 0;
		for (String param : transaction.getResponseParameters()) {
			checksumString = checksumString + param + "=" + request.getParameter(param);
			checksumString = checksumString + "&";
			// This will create the checksum string against every parameter.
			parameter[n] = request.getParameter(param);
			n += 1;
		}
		for (String string : parameter) {
			System.out.println("Array Parameters: " + string);
		}

		Boolean verifyChecksum = checksumGenerator.verifyChecksum(paymentSettingBag.getSecretKey(), checksumString,
				request.getParameter("checksum"));
		verifiedChecksum = verifyChecksum;
		checksum = request.getParameter("checksum");
		responseParameters = transaction.getResponseParameters();

		return "redirect:/zaakpay/cab/response/api";
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/bus/response/api", method = { RequestMethod.GET })
	public String zaakpayResponseSebusOrderAPI(HttpServletRequest request, Model model, HttpServletResponse response)
			throws Exception {

		String[] ids = parameter[8].split("ID");
		Customer customer = customerRepository.findById(Integer.parseInt(ids[2])).get();
		BusOrder rideOrder = busOrderRepo.findById(Integer.parseInt(ids[1])).get();
		List<BusOrder> rideOrders = new ArrayList<>();
		rideOrders.add(rideOrder);

		boolean isProcessed = false;
		System.out.println(parameter);
		com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		double amountIntRech = Double.parseDouble(parameter[0]) / 100;

		if (parameter[9].contains("Not Found") && parameter[10].equals("unknown")) {
			updateBusOrder(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			updateBusOrder(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			updateBusOrder(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Customer cancelled transaction. Transaction has failed")) {
			updateBusOrder(rideOrder, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			updateBusOrder(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			updateBusOrder(rideOrder, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains(
				"Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			updateBusOrder(rideOrder, OrderStatus.CANCELLED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			updateBusOrder(rideOrder, OrderStatus.FAILED);
			isProcessed = false;
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("100")) {
			if (parameter[12].contains("The transaction was completed successfully.")
				|| parameter[12].contains("Transaction has been settled.")) {
//			totalTransactionService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), false, true,
//					rideOrders, selfTrans.getId(), OrderStatus.SUCCESSFULL);
			updateBusOrder(rideOrder, OrderStatus.SUCCESSFULL);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
//			paymentService.sendSuccessEmail(customer, selfTrans.getPgTransId(), isProcessed, rideOrder);
		}
		} else if (parameter[12].contains("The transaction was completed successfully.")
				|| parameter[12].contains("Transaction has been settled.")) {
//			totalTransactionService.createTotalTransactionRide(customer, rideOrder.getTotalAmount(), false, true,
//					rideOrders, selfTrans.getId(), OrderStatus.SUCCESSFULL);
			updateBusOrder(rideOrder, OrderStatus.SUCCESSFULL);
			isProcessed = true;
			model.addAttribute("paymentSuccess", parameter[12]);
//			paymentService.sendSuccessEmail(customer, selfTrans.getPgTransId(), isProcessed, rideOrder);
		}

		System.out.println(parameter);

		model.addAttribute("customer", customer);
		model.addAttribute("amountRecharged", amountIntRech);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);

		return "bus/order/response-bus-order-api";

	}
	
	//Methods

	private void updateBusOrder(BusOrder rideOrder, OrderStatus orderStatus) {
		rideOrder.setOrderStatus(orderStatus);
		busOrderRepo.save(rideOrder);
		System.out.println("0000000000000000000000000000000000000000000 " + rideOrder.getOrderStatus());
	}
	
	private void updateRide(RideOrder rideOrder, OrderStatus orderStatus) {
		rideOrder.setStatus(orderStatus);
		rideOrderRepo.save(rideOrder);
		System.out.println("0000000000000000000000000000000000000000000 " + rideOrder.getStatus());
	}

}
