package com.easygofly.site.wallet;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.easygofly.entity.BusOrder;
import com.easygofly.entity.Customer;
import com.easygofly.entity.HotelOrder;
import com.easygofly.entity.Order;
import com.easygofly.entity.RechargeHistory;
import com.easygofly.entity.RechargeHistoryStatus;
import com.easygofly.entity.Wallet;
import com.easygofly.site.Utility;
import com.easygofly.site.setting.EmailSettingBag;
import com.easygofly.site.setting.SettingService;

@Service
public class WalletService {

	@Autowired
	WalletRepository walletRepo;
	@Autowired
	RechargeHistoryRepository rechargeHistoryRepo;
	@Autowired
	private SettingService settingService;

	public Wallet findbyCustomer(Customer customer) {
		Wallet wallet = walletRepo.findByCustomer(customer);
		return wallet;
	}

	public Wallet setTempValue(Customer customer, Integer paisaValue) {
		Wallet wallet = customer.getWallet();
		wallet.setTempValue(paisaValue);

		return walletRepo.save(wallet);
	}

	public RechargeHistory createRechargeHistoryByZaakpay(Customer customer, String transId, String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		RechargeHistory findRechargeHistory = rechargeHistoryRepo.findByZaakpayIdByCreatedAtAsc(wallet, zaakpayTransId);
		if (findRechargeHistory == null) {

			RechargeHistory rechargeHistory = new RechargeHistory();
			rechargeHistory.setDate(new Date());
			rechargeHistory.setWallet(wallet);
			rechargeHistory.setRechargeAmount(wallet.getTempValue());
			rechargeHistory.setTransaction(transId);
			rechargeHistory.setZaakpaytransactionId(zaakpayTransId);

			return rechargeHistoryRepo.save(rechargeHistory);
		} else {

			return findRechargeHistory;
		}
	}

	public RechargeHistory updateRechargeHistoryStatus(RechargeHistory rechargeHistory,
			RechargeHistoryStatus rechargeHistoryStatus) {
		rechargeHistory.setRechargeHistoryStatus(rechargeHistoryStatus);

		return rechargeHistoryRepo.save(rechargeHistory);
	}

	public Wallet updateWalletValue(Customer customer, RechargeHistory rechargeHistory) {
		Wallet wallet = customer.getWallet();

		wallet.setBalance(wallet.getBalance() + rechargeHistory.getRechargeAmount());
		wallet.setTempValue(0);

		return walletRepo.save(wallet);
	}

	public List<RechargeHistory> listAllRechargeHistory(Wallet wallet, Sort sort) {
		List<RechargeHistory> rechargeHistories = rechargeHistoryRepo.findByWallet(wallet, sort);
		return rechargeHistories;
	}

	public Wallet updateWalletBalanceByOrder(Customer customer, Order order, String transId, String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		double dblOrder100 = order.getPrice() * 100;
		Integer intOrder100 = (int) dblOrder100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() - intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	public Wallet updateWalletBalanceByHotelOrder(Customer customer, HotelOrder order, String transId,
			String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		double dblOrder100 = order.getPrice() * 100;
		Integer intOrder100 = (int) dblOrder100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() - intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	public Wallet updateWalletBalanceByBusOrder(Customer customer, BusOrder order, String transId,
			String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		double dblOrder100 = order.getPrice() * 100;
		Integer intOrder100 = (int) dblOrder100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() - intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	public Wallet cancelWalletBalanceByBusOrder(Customer customer, BusOrder order, String transId,
			String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		double dblOrder100 = order.getPrice() * 100;
		Integer intOrder100 = (int) dblOrder100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() + intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	public Wallet cancelWalletBalanceByOrder(Customer customer, Order order, String transId, String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		double dblOrder100 = order.getPrice() * 100;
		Integer intOrder100 = (int) dblOrder100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() + intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	public Wallet cancelWalletBalanceByHotelOrder(Customer customer, HotelOrder order, String transId,
			String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		double dblOrder100 = order.getPrice() * 100;
		Integer intOrder100 = (int) dblOrder100;
		Integer intOrder = (int) order.getPrice();
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() + intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.FAILED);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	public Wallet updateWalletBalanceByOrderReturn(Customer customer, Order order1, Order order2, String transId,
			String zaakpayTransId) {
		Wallet wallet = customer.getWallet();
		Integer intOrder100 = ((int) order1.getPrice() + (int) order2.getPrice()) * 100;
		Integer intOrder = ((int) order1.getPrice() + (int) order2.getPrice());
		if (wallet.getBalance() >= intOrder100) {
			System.out.println((wallet.getBalance() / 100) + " is BIGGER than " + intOrder);
			wallet.setBalance(wallet.getBalance() - intOrder100);
			wallet.setTempValue(intOrder100);

			RechargeHistory rechargeHistory = createRechargeHistoryByZaakpay(customer, transId, zaakpayTransId);
			updateRechargeHistoryStatus(rechargeHistory, RechargeHistoryStatus.SUCCESSFULL);

			return walletRepo.save(wallet);
		} else {
			System.out.println((wallet.getBalance() / 100) + " is SMALLER than " + intOrder);
			return null;
		}
	}

	// Email Service

	public void sendSuccessEmail(Customer customer, String trn, String amount, String balance, boolean isProcessed)
			throws UnsupportedEncodingException, MessagingException {

		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);

		String toAddress = customer.getEmail();
		String subject = emailSettings.getWalletRechargeSubject();
		String content = emailSettings.getWalletRechargeContent();

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());

		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy - hh:mm a");
		dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String formatedDate = dateFormat.format(date);

		if (formatedDate.contains("am")) {
			formatedDate = formatedDate.replace("am", "AM");
		} else if (formatedDate.contains("pm")) {
			formatedDate = formatedDate.replace("pm", "PM");
		}

		if (isProcessed) {
			subject = subject.replace("[[SSS]]", "successful");
			content = content.replace("[[SSS]]", "successful");
		} else {
			subject = subject.replace("[[SSS]]", "failure");
			content = content.replace("[[SSS]]", "failure");
		}

		subject = subject.replace("[[DDD]]", formatedDate);

		content = content.replace("[[NAME]]", customer.getFullName());

		content = content.replace("[[AMT]]", amount);

		content = content.replace("[[BLC]]", balance);

		content = content.replace("[[TRN]]", trn);

		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);

		mailSender.send(message);

		System.out.println("To Address: " + toAddress);
	}

}
