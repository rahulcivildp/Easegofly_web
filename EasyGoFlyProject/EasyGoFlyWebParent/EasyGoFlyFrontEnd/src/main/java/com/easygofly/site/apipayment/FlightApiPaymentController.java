package com.easygofly.site.apipayment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.TotalTransaction;
import com.easygofly.site.customer.CustomerRepository;
import com.easygofly.site.flight.order.OrderRepository;
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
public class FlightApiPaymentController {
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	WalletService walletService;
	@Autowired
	private SettingService settingService;
	@Autowired
	WalletController walletController;
	@Autowired
	TransactionService transactionService;
	@Autowired
	TransactionRepository transactionRepository;
	@Autowired
	TotalTransactionService totalTransactionService;
	@Autowired
	private OrderRepository orderRepo;

	private String[] parameter = new String[20];
	private String checksum;
	private Boolean verifiedChecksum;
	private String[] responseParameters;

	// Flight One-way Payment API

	@GetMapping("/flight-payment/api/{order_id}/{cust_id}")
	public String flightPaymentAPI(@PathVariable("order_id") Integer order_id,
			@PathVariable("cust_id") Integer cust_id, HttpServletRequest request) {
		System.out.println(order_id);
		System.out.println(cust_id);
		
		return "redirect:/flight-payment/api/confirm-" + order_id + "/" + cust_id;
	}
	


	@GetMapping("/flight-payment/api/confirm-{order_id}/{cust_id}")
	public String flightPaymentReturnAPI(@PathVariable("order_id") Integer order_id,
			@PathVariable("cust_id") Integer cust_id, HttpServletRequest request, Model model) {

		Customer customer = customerRepository.findById(cust_id).get();
		Order order = orderRepo.findById(order_id).get();

		System.out.println(order_id);
		System.out.println(cust_id);

		if (customer != null) {
			zaakpayPaymentAPI(request, customer, order, model);
		}

		return "order/flight-confirm-api";
	}

	private void zaakpayPaymentAPI(HttpServletRequest request, Customer customer, Order order, Model model) {
		PaymentSettingBag paymentSettingBag = settingService.getPaymentSettings();

		/* ------ ZAAKPAY -------- */ /**/
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("hhmmss");
		String strDate1 = dateFormat1.format(date);
		String strDate2 = dateFormat2.format(date);

		String orderString = "EGF" + strDate1 + "T" + strDate2 + "ID" + order.getId() + "ID" + customer.getId();
		// String orderString = "TRN";
		Integer intAmount = (int) (order.getPrice() * 100);
		String amount = "" + intAmount;
//		 String amount = "100";

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
			ZaakpayApiRequestParameters processPayment = transaction.processFlightOnewayPaymentAPI(orderString, amount,
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
	@RequestMapping(value = "/zaakpay/flight/response/api", method = { RequestMethod.POST })
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

		return "redirect:/zaakpay/flight/response/api";
	}

	@CrossOrigin(origins = { "https://easegofly.com/" })
	@RequestMapping(value = "/zaakpay/flight/response/api", method = { RequestMethod.GET })
	public String zaakpayResponseSeAPI(HttpServletRequest request, Model model, HttpServletResponse response)
			throws Exception {

		String[] ids = parameter[8].split("ID");
		Customer customer = customerRepository.findById(Integer.parseInt(ids[2])).get();
		Order order = orderRepo.findById(Integer.parseInt(ids[1])).get();

		System.out.println(parameter);
		com.easygofly.entity.Transaction selfTrans = transactionService.createTransaction(customer, parameter);

		TotalTransaction trans = totalTransactionService.createTotalTransaction(customer, Double.parseDouble(selfTrans.getAmount()), false, true,
				null, null, null, selfTrans.getId(), OrderStatus.NEW);
		
		double amountIntRech = Double.parseDouble(parameter[0]) / 100;

		if (parameter[9].equals("Not Found") && parameter[10].equals("unknown")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.FAILED);
			order.setOrderStatus(OrderStatus.FAILED);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12]
				.contains("Unfortunately the transaction has failed.Please try again. Transaction has failed")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.FAILED);
			order.setOrderStatus(OrderStatus.FAILED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Unfortunately the transaction has failed.Please try again.")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.FAILED);
			order.setOrderStatus(OrderStatus.FAILED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains("Customer cancelled transaction. Transaction has failed")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.FAILED);
			order.setOrderStatus(OrderStatus.FAILED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].equals("") || parameter[12] == null || parameter[9] == null) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.FAILED);
			order.setOrderStatus(OrderStatus.FAILED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains(
				"Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.CANCELLED);
			order.setOrderStatus(OrderStatus.CANCELLED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[12].contains(
				"Your Bank has declined this transaction please Retry this payment with another pay method.")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.CANCELLED);
			order.setOrderStatus(OrderStatus.CANCELLED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("1017")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.CANCELLED);
			order.setOrderStatus(OrderStatus.CANCELLED);
			orderRepo.save(order);
			model.addAttribute("paymentCancelled", parameter[12]);
		} else if (parameter[11].contains("100")) {
			if (parameter[12].contains("The transaction was completed successfully.")
					|| parameter[12].equals("Transaction has been settled.")) {
				totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.SUCCESSFULL);
				order.setOrderStatus(OrderStatus.SUCCESSFULL);
				orderRepo.save(order);
				model.addAttribute("paymentSuccess", parameter[12]);
			}
		} else if (parameter[12].contains("The transaction was completed successfully.")
				|| parameter[12].equals("Transaction has been settled.")) {
			totalTransactionService.updateTotalTransactionStatus(trans, OrderStatus.SUCCESSFULL);
			order.setOrderStatus(OrderStatus.SUCCESSFULL);
			orderRepo.save(order);
			model.addAttribute("paymentSuccess", parameter[12]);
		}

		System.out.println(parameter);

		model.addAttribute("customer", customer);
		model.addAttribute("amountRecharged", amountIntRech);
		model.addAttribute("checksum", checksum);
		model.addAttribute("verifyChecksum", verifiedChecksum);
		model.addAttribute("responseParameters", responseParameters);

		return "zaakpay/flight-oneway-api";

	}

}
