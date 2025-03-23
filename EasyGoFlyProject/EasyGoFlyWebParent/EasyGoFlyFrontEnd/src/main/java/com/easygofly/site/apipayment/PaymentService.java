package com.easygofly.site.apipayment;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Customer;
import com.easygofly.entity.RideOrder;
import com.easygofly.site.Utility;
import com.easygofly.site.setting.EmailSettingBag;
import com.easygofly.site.setting.SettingService;

@Service
public class PaymentService {
	@Autowired private SettingService settingService;

	public void sendSuccessEmail (Customer customer, String trn, boolean isProcessed, RideOrder rideOrder) throws UnsupportedEncodingException, MessagingException {

		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);

		String toAddress = customer.getEmail();
		String subject = "Hey, " + customer.getFirstName() + "! 🚖 Your Cab Booking Confirmation – Ride Details Inside!";
		
		String content = "<!DOCTYPE html>\r\n"
				+ "<html>\r\n"
				+ "<head>\r\n"
				+ "    <meta charset=\"UTF-8\">\r\n"
				+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
				+ "    <title>Cab Order Confirmation</title>\r\n"
				+ "    <style>\r\n"
				+ "        body {\r\n"
				+ "            font-family: 'Poppins', sans-serif;\r\n"
				+ "            background-color: #f4f4f4;\r\n"
				+ "            margin: 0;\r\n"
				+ "            padding: 0;\r\n"
				+ "        }\r\n"
				+ "        .container {\r\n"
				+ "            max-width: 600px;\r\n"
				+ "            margin: 20px auto;\r\n"
				+ "            background: #ffffff;\r\n"
				+ "            padding: 20px;\r\n"
				+ "            border-radius: 15px;\r\n"
				+ "            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);\r\n"
				+ "        }\r\n"
				+ "        .header {\r\n"
				+ "            text-align: center;\r\n"
				+ "            background: linear-gradient(135deg, #007bff, #00d4ff);\r\n"
				+ "            color: #ffffff;\r\n"
				+ "            padding: 15px;\r\n"
				+ "            border-radius: 15px 15px 0 0;\r\n"
				+ "        }\r\n"
				+ "        .details {\r\n"
				+ "            margin: 20px 0;\r\n"
				+ "            padding: 15px;\r\n"
				+ "            background: #fafafa;\r\n"
				+ "            border-radius: 10px;\r\n"
				+ "            font-size: 16px;\r\n"
				+ "            color: #333;\r\n"
				+ "        }\r\n"
				+ "        .details p {\r\n"
				+ "            margin: 8px 0;\r\n"
				+ "            line-height: 1.6;\r\n"
				+ "        }\r\n"
				+ "        .highlight {\r\n"
				+ "            font-weight: bold;\r\n"
				+ "            color: #007bff;\r\n"
				+ "        }\r\n"
				+ "        .footer {\r\n"
				+ "            text-align: center;\r\n"
				+ "            font-size: 14px;\r\n"
				+ "            color: #666;\r\n"
				+ "            margin-top: 20px;\r\n"
				+ "        }\r\n"
				+ "        .driver-card, .cab-card {\r\n"
				+ "            padding: 15px;\r\n"
				+ "            border-radius: 10px;\r\n"
				+ "            background: #fff;\r\n"
				+ "            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\r\n"
				+ "            margin-top: 20px;\r\n"
				+ "            text-align: center;\r\n"
				+ "        }\r\n"
				+ "        .driver-photo, .cab-photo {\r\n"
				+ "            width: 100px;\r\n"
				+ "            height: 100px;\r\n"
				+ "            border-radius: 50%;\r\n"
				+ "            object-fit: cover;\r\n"
				+ "            margin-bottom: 10px;\r\n"
				+ "        }\r\n"
				+ "    </style>\r\n"
				+ "</head>\r\n"
				+ "<body>\r\n"
				+ "    <div class=\"container\">\r\n"
				+ "        <div class=\"header\">\r\n"
				+ "            <h2>🚖 Cab [[SSS]]</h2>\r\n"
				+ "        </div>\r\n"
				+ "        <p>Dear <span class=\"highlight\">" + customer.getFullName() + "</span>,</p>\r\n"
				+ "        <p>Thank you for booking your ride with us! Here are your details:</p>\r\n"
				+ "        <div class=\"details\">\r\n"
				+ "            <p><strong>Transaction ID:</strong> " + trn + "</p>\r\n"
				+ "            <p><strong>Status:</strong> " + rideOrder.getStatus().toString() + "</p>\r\n"
				+ "            <p><strong>Order Created:</strong> " + rideOrder.getDate() + "</p>\r\n"
				+ "            <p><strong>Order Name:</strong> " + rideOrder.getOrderName() + "</p>\r\n"
				+ "            <p><strong>Base Fare:</strong> ₹" + String.valueOf(rideOrder.getBaseFare()) + "</p>\r\n"
				+ "            <p><strong>Taxes:</strong> ₹" + String.valueOf(rideOrder.getTaxes()) + "</p>\r\n"
				+ "            <p><strong>Discount:</strong> ₹" + String.valueOf(rideOrder.getDiscount()) + "</p>\r\n"
				+ "            <p><strong>Convenience Fee:</strong> ₹" + String.valueOf(rideOrder.getConvenience()) + "</p>\r\n"
				+ "            <p><strong>Total Fare:</strong> rupees ₹" + String.valueOf(rideOrder.getTotalAmount()) + "</p>\r\n"
				+ "        </div>\r\n"
				+ "        <div class=\"driver-card\">\r\n"
				+ "            <img class=\"driver-photo\" src=\"" + rideOrder.getDriverId().getPhotosImagePath() + "\" alt=\"Driver Photo\">\r\n"
				+ "            <p><strong>Driver:</strong> " + rideOrder.getDriverId().getName() + "</p>\r\n"
				+ "            <p><strong>Rating:</strong> ⭐ " + String.valueOf(rideOrder.getDriverId().getRating()) + "</p>\r\n"
				+ "            <p><strong>Experience:</strong> " + String.valueOf(rideOrder.getDriverId().getExperience()) + " years</p>\r\n"
				+ "            <p><strong>Contact:</strong> " + rideOrder.getDriverId().getContact() + "</p>\r\n"
				+ "            <p><strong>Location:</strong> " + rideOrder.getDriverId().getLocation() + "</p>\r\n"
				+ "        </div>\r\n"
				+ "        <div class=\"cab-card\">\r\n"
				+ "            <img class=\"cab-photo\" src=\"" + rideOrder.getDriverId().getCab().getPhotosImagePath() + "\" alt=\"Cab Photo\">\r\n"
				+ "            <p><strong>Cab:</strong> " + rideOrder.getDriverId().getCab().getName() + "</p>\r\n"
				+ "            <p><strong>Type:</strong> " + rideOrder.getDriverId().getCab().getType() + "</p>\r\n"
				+ "            <p><strong>Seating:</strong> " + String.valueOf(rideOrder.getDriverId().getCab().getSeating()) + " passengers</p>\r\n"
				+ "            <p><strong>Fuel Type:</strong> " + rideOrder.getDriverId().getCab().getFuelType() + "</p>\r\n"
				+ "            <p><strong>Color:</strong> " + rideOrder.getDriverId().getCab().getColor() + "</p>\r\n"
				+ "            <p><strong>Max Speed:</strong> " + String.valueOf(rideOrder.getDriverId().getCab().getMaxSpeed()) + " km/h</p>\r\n"
				+ "            <p><strong>Air Conditioning:</strong> " + rideOrder.getDriverId().getCab().getAirConditioning() + "</p>\r\n"
				+ "            <p><strong>WiFi:</strong> " + rideOrder.getDriverId().getCab().getWifi() + "</p>\r\n"
				+ "            <p><strong>Features:</strong> " + rideOrder.getDriverId().getCab().getFeatures() + "</p>\r\n"
				+ "        </div>\r\n"
				+ "        <p>We hope you have a smooth and comfortable ride!</p>\r\n"
				+ "        <p>Best Regards,<br>🚖 APNARIDES</p>\r\n"
				+ "        <div class=\"footer\">\r\n"
				+ "            <p>&copy; 2025 <a href='htpps://webkoro.com'>Webkoro</a>. All rights reserved.</p>\r\n"
				+ "            <br>\r\n"
				+ "            <a href='htpps://apnarides.com'>Apnarides.com.</a>\r\n"
				+ "        </div>\r\n"
				+ "    </div>\r\n"
				+ "</body>\r\n"
				+ "</html>\r\n"
				+ "";
	

		MimeMessage message= mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
	    
	    if (isProcessed) {
			content = content.replace("[[SSS]]", "✅ ORDER CONFIRMED");
		} else {
			content = content.replace("[[SSS]]", "❌ ORDER FAILED");
		}
		
		
		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);
		
		mailSender.send(message);
		
		System.out.println("To Address: " + toAddress);
	}

}
