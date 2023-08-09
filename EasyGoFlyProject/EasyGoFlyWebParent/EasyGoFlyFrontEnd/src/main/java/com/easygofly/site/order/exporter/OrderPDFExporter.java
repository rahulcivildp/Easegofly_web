package com.easygofly.site.order.exporter;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Brand;
import com.easygofly.entity.City;
import com.easygofly.entity.Order;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode39;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class OrderPDFExporter extends AbstractOrderExporter {

	public void export(Order order, HttpServletResponse response, City cityOne, City cityTwo, String logoLink, List<TravellerDetail> travellerDetails, String faviconLink, Brand brand) throws Exception {
		super.setResponseHeader(response, "application/pdf", ".pdf");
		
		Document document = new Document(PageSize.A4);
		PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
		document.open();
		
		PdfContentByte cb = writer.getDirectContent();

		ProductDetail flight = order.getProductDetail();
		
		headerTable(new PdfPTable(3), document, order);
		addressAndBarCode(cb, new PdfPTable(3), document, order, faviconLink);
		flightItinary(new PdfPTable(3), document, order, cityOne, cityTwo);
		flightDetailsHeader(new PdfPTable(1), document, order, cityOne, cityTwo, brand);
		flightDetails(new PdfPTable(5), document, order, cityOne, cityTwo, brand);
		travelerDetailsHeader(new PdfPTable(2), document, order);
		travelerTableHeader(new PdfPTable(6), document, order);
		
		for (TravellerDetail traveler : travellerDetails) {
			travelerTableBody(new PdfPTable(6), document, order, traveler, flight);
		}
		travelerBorder(new PdfPTable(1), document);
		totalFareTable(new PdfPTable(2), document, order);
		extraDetailsAfterPrice(new PdfPTable(2), document, order, logoLink);
		travellerInfo(cb, new PdfPTable(3), document, order);
		helpInfo(new PdfPTable(1), document);
		additionalInfo(new PdfPTable(1), document);
		footerDialog(new PdfPTable(1), document);
		footerInfo(new PdfPTable(2), document);
		
		document.close();
		
	}	
	
	private void headerTable(PdfPTable table, Document document, Order order) {
		
		table.setWidthPercentage(100f);
		table.setSpacingBefore(10);
		table.setWidths(new float[] {4.8f, 6.5f, 3.2f});
		
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.WHITE);
		cell.setPadding(5);
		
		Date date = order.getCreatedTime();
        DateFormat date_format = new SimpleDateFormat("yyyy-MMM-dd", Locale.ENGLISH);
        String date_string = date_format.format(date);
        
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(12);
		font.setColor(Color.BLACK);
		
		Font dateFont = FontFactory.getFont(FontFactory.HELVETICA);
		dateFont.setSize(8);
		dateFont.setColor(Color.BLACK);
		
		cell.setBorderColor(Color.WHITE);
		cell.setPhrase(new Phrase(date_string, dateFont));
		table.addCell(cell);

		cell.setBorderColor(Color.WHITE);
		cell.setPhrase(new Phrase("Easegofly - Flight booking portal", font));
		table.addCell(cell);

		cell.setBorderColor(Color.WHITE);
		cell.setPhrase(new Phrase("", font));
		table.addCell(cell);
		
		document.add(table);
	}
	
	private void addressAndBarCode(PdfContentByte cb, PdfPTable table, Document document, Order order, String faviconLink) throws Exception {

		table.setWidthPercentage(100f);
		table.setSpacingBefore(10);
		table.setWidths(new float[] {1.5f, 9.0f, 4.0f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(10);
		font.setColor(Color.BLACK);
		
		Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontBold.setSize(12);
		fontBold.setColor(Color.BLACK);
		
		//1st cell...
		Path path = Paths.get(".." + faviconLink);
		Image imgFavicon = Image.getInstance(path.toFile().getAbsolutePath());

		PdfPCell cell = new PdfPCell(imgFavicon, true);
		addressCellModify(cell);
		cell.setPadding(5);
		table.addCell(cell);
		
		//2nd Cell...
		cell = new PdfPCell();
		addressCellModify(cell);
		cell.setPadding(5);
		Paragraph p = new Paragraph("");
		Chunk bold = new Chunk(" \s\sEASEGOFLY TEAM\r\n", fontBold);
		Chunk normal = new Chunk(" \s\sKOLKATA 700099, WEST BENGAL, INDIA.\r\n \s\s8348000139, support@easegofly.com", font);
		p.add(bold);
		p.add(normal);
		
		cell.setPhrase(p);
		table.addCell(cell);
		
		//3rd Cell...
		ProductDetail flight = order.getProductDetail();
		String pnr = flight.getPnr();
		
		Barcode39 code39 = new Barcode39();
		code39.setCode(pnr.toUpperCase());
        code39.setStartStopText(false);
        Image image39 = code39.createImageWithBarcode(cb, null, null);
        
        cell = new PdfPCell();
		addressCellModify(cell);
        cell.setPhrase(new Phrase(new Chunk(image39, 0, 0)));
        table.addCell(cell);
		
		document.add(table);
	}

	private void addressCellModify(PdfPCell cell) {
		cell.setFixedHeight(60.0f);
		cell.setBackgroundColor(Color.WHITE);
		cell.setBorderColor(Color.WHITE);
	}
	
	private Image imageLogo(Order order, Brand brand) throws BadElementException, IOException {
		Path path = Paths.get(".." + brand.getPhotosImagePath());
		
		Image img = Image.getInstance(path.toFile().getAbsolutePath());
		return img;
	}
	
	private void flightItinary(PdfPTable table, Document document, Order order, City cityOne, City cityTwo ) {
		
		table.setWidthPercentage(100f);
		table.setSpacingBefore(10);
		table.setWidths(new float[] {9.8f, 0.5f, 4.2f});
		
		PdfPCell cell = new PdfPCell();
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(10);
		font.setColor(Color.BLACK);
		
		Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontBold.setSize(10);
		fontBold.setColor(Color.BLACK);
		
		//1st cell
		ProductDetail flight = order.getProductDetail();
		Paragraph p = new Paragraph("");
		Chunk normal1 = new Chunk("Flight Itineray: ", font);
		Chunk boldCity1 = new Chunk(cityOne.getCityName(), fontBold);
		Chunk normal2 = new Chunk(" to ", font);
		Chunk boldCity2 = new Chunk(cityTwo.getCityName(), fontBold);
		
		Date date = flight.getDate();   
	    DateFormat dateFormat1 = new SimpleDateFormat("EE, dd-MMM-yyyy", Locale.ENGLISH); 
	    String fullDate = dateFormat1.format(date);
	    
		Chunk normal3 = new Chunk(" | " + fullDate, font);
		
		p.add(normal1);
		p.add(boldCity1);
		p.add(normal2);
		p.add(boldCity2);
		p.add(normal3);
		
		cell.setBorderColor(Color.WHITE);
		cell.setPhrase(p);
		table.addCell(cell);
		
		//2nd cell
		cell.setBorderColor(Color.WHITE);
		cell.setPhrase(new Phrase("", font));
		table.addCell(cell);
				
		//3rd cell
		Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA);
		fontSmall.setSize(9);
		fontSmall.setColor(Color.BLACK);
		
		Date date1 = order.getCreatedTime();  
	    DateFormat dateFormat3 = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);  
	    String strDate = "Issued Date: " + dateFormat3.format(date1); 
	    
		cell.setBorderColor(Color.WHITE);
		cell.setPhrase(new Phrase(strDate, fontSmall));
		table.addCell(cell);

		document.add(table);
	}
	
	private void flightDetailsHeader(PdfPTable table, Document document, Order order, City cityOne, City cityTwo, Brand brand) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(10);
		table.setWidths(new float[] {14.5f});
		
		PdfPTable tableNested = new PdfPTable(4); 
		tableNested.setWidths(new float[] {5.5f, 5.0f, 2.2f, 0.8f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(12);
		font.setColor(Color.BLACK);
		
		Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontBold.setSize(12);
		fontBold.setColor(Color.BLACK);
		
		//1st cell
		PdfPCell cell = new PdfPCell();
		cellModifyStyle(cell);
		Paragraph p = new Paragraph("");
		Chunk normal = new Chunk("Passenger CTCM: ", font);
		Chunk bold = new Chunk(order.getPhoneNumber().toString(), fontBold);
		p.add(normal);
		p.add(bold);
		cell.setPhrase(p);
		
		tableNested.addCell(cell);
		
		//2nd cell
		ProductDetail flight = order.getProductDetail();
		cell = new PdfPCell();
		Paragraph p2 = new Paragraph("");
		Chunk normal2 = new Chunk("AIRLINE PNR: ", font);
		Chunk bold2 = new Chunk(flight.getPnr(), fontBold);
		p2.add(normal2);
		p2.add(bold2);
		cell.setPhrase(p2);
		cellModifyStyle(cell);
		tableNested.addCell(cell);
		
		//3rd cell
		cell = new PdfPCell();
		cellModifyStyle(cell);
		tableNested.addCell(cell);
		
		//4th cell
		cell = new PdfPCell(imageLogo(order, brand), true);
		cellModifyStyle(cell);
		tableNested.addCell(cell);
		
		table.addCell(tableNested);
		
		document.add(table);
	}

	private void cellModifyStyle(PdfPCell cell) {
		Color color = new Color(175, 216, 247);
		cell.setBorderColor(color);
		cell.setBackgroundColor(color);
		cell.setFixedHeight(19.0f);
	}

	private void flightDetails(PdfPTable table, Document document, Order order, City cityOne, City cityTwo, Brand brand) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setWidths(new float[] {2.6f, 3.2f, 2.9f, 3.2f, 2.6f});
		table.setHorizontalAlignment(Element.ALIGN_CENTER);
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(10);
		font.setColor(Color.BLACK);
		
		Font fontFancy = FontFactory.getFont(FontFactory.HELVETICA);
		fontFancy.setSize(9);
		fontFancy.setColor(Color.BLACK);
		
		Font fontRed = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontRed.setSize(10);
		fontRed.setColor(Color.RED);
		
		Font fontGreen = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontGreen.setSize(10);
		fontGreen.setColor(Color.GREEN);
		
		Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontBold.setSize(9);
		fontBold.setColor(Color.BLACK);

		//1st cell...............................................
		PdfPCell cell = new PdfPCell(imageLogo(order, brand), true);
		cell.setFixedHeight(100.0f);
		ProductDetail flight = order.getProductDetail();
		
		Image img = imageLogo(order, brand);
        img.scaleAbsolute(40, 40);
		
        Chunk ck = new Chunk("\n");
		Chunk ck1 = new Chunk(img, 0, 0);
		Chunk ck2 = new Chunk("\n\n" + brand.getName() + "\s\n", fontBold);
		Chunk ck3 = new Chunk(flight.getFlightNum(), fontFancy);
		
		Phrase ph = new Phrase();
		ph.add(ck); ph.add(ck1); ph.add(ck2); ph.add(ck3);
		
		cell.setPhrase(ph);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		//2nd cell...............................................
		cell = new PdfPCell();
		
		Date date = flight.getDate();  
	    DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH); 
	    String fullDate = dateFormat1.format(date);
	    String dayWeekText = new SimpleDateFormat("EE").format(date);
	    
		Chunk dp = new Chunk("\n");
		Chunk dp1 = new Chunk("DEPARTURE: ", fontFancy);
		Chunk dp2 = new Chunk(cityOne.getCityName() + "\n", fontBold);
		Chunk dp3 = new Chunk(cityOne.getCode() + " - ", fontBold);
		Chunk dp4 = new Chunk(flight.getDepTime() + ", " + dayWeekText + "\n", fontBold);
		Chunk dp5 = new Chunk(fullDate + "\n", font);
		
		Path path = Paths.get("../pdf-images/flight-up.png");
		Image flightUp = Image.getInstance(path.toFile().getAbsolutePath());
		flightUp.scaleAbsolute(30, 30);
		
		Chunk dp6 = new Chunk(flightUp, 0, 0);
		Chunk dp7 = new Chunk("\nTERMINAL: " + flight.getTerminalDep(), fontBold);
		
		Phrase deph = new Phrase();
		deph.add(dp); deph.add(dp1); deph.add(dp2); deph.add(dp3); deph.add(dp4); deph.add(dp5); deph.add(dp6); deph.add(dp7); 
		
		cell.setPhrase(deph);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		//3rd cell...............................................
		cell = new PdfPCell();
		
		Integer durationHours = flight.getDuration()/60;
		Integer durationMinutes = flight.getDuration()%60;
		String durationHoursWord = "0" + durationHours.toString();
		String durationMinutesWord = durationMinutes.toString();
		
		Chunk duration = new Chunk("\n");
		Chunk duration1;
		
		if (durationMinutes < 10) {
			duration1 = new Chunk("\n" + durationHoursWord + "HRS : " + durationMinutesWord + "0MINS\n\n", fontFancy);
		} else {
			duration1 = new Chunk("\n" + durationHoursWord + "HRS : " + durationMinutesWord + "MINS\n\n", fontFancy);
		}
		
		Chunk duration2;
		
		Integer stopsInt = flight.getStopNum();
		if (stopsInt != 0) {
			String stops = stopsInt.toString();
			duration2 = new Chunk("STOPS: " + stops + "\n", fontRed);
		} else {
			duration2 = new Chunk("NON STOP\n", fontRed);
		}
		
		Chunk duration3 = new Chunk(flight.getJourneyClass(), fontFancy);
		
		Phrase dura = new Phrase();
		dura.add(duration); dura.add(duration1); dura.add(duration2); dura.add(duration3);
		
		cell.setPhrase(dura);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		//4th cell...............................................
		cell = new PdfPCell();
		
		Chunk arr = new Chunk("\n");
		Chunk arr1 = new Chunk("ARRIVAL: ", fontFancy);
		Chunk arr2 = new Chunk(cityTwo.getCityName() + "\n", fontBold);
		Chunk arr3 = new Chunk(cityTwo.getCode() + " - ", fontBold);
		Chunk arr4 = new Chunk(flight.getArrTime() + ", " + dayWeekText + "\n", fontBold);
		Chunk arr5 = new Chunk(fullDate + "\n", font);
		
		Path path2 = Paths.get("../pdf-images/flight-down.png");
		Image flightDown = Image.getInstance(path2.toFile().getAbsolutePath());
		flightDown.scaleAbsolute(30, 30);
		
		Chunk arr6 = new Chunk(flightDown, 0, 0);
		Chunk arr7 = new Chunk("\nTERMINAL: " + flight.getTerminalArr(), fontBold);
		
		Phrase arrPhr = new Phrase();
		arrPhr.add(arr); arrPhr.add(arr1); arrPhr.add(arr2); arrPhr.add(arr3); arrPhr.add(arr4); arrPhr.add(arr5); arrPhr.add(arr6); arrPhr.add(arr7); 
		
		cell.setPhrase(arrPhr);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		//5th cell...............................................
		cell = new PdfPCell();
		Chunk pnr = new Chunk("\n");
		Chunk pnr1 = new Chunk("PNR STATUS\n", fontBold);
		
		String status = order.getOrderStatus().toString();
		Chunk pnr2 = new Chunk(status + "\n\n", fontGreen);
		
		Path path3 = Paths.get("../pdf-images/demo-ticket.png");
		Image demoTicket = Image.getInstance(path3.toFile().getAbsolutePath());
		demoTicket.scaleAbsolute(70, 23);
		
		Chunk pnr3 = new Chunk(demoTicket, 0, 0);
		
		Phrase statusPhr = new Phrase();
		statusPhr.add(pnr); statusPhr.add(pnr1); statusPhr.add(pnr2); statusPhr.add(pnr3);
		
		cell.setPhrase(statusPhr);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
		
		document.add(table);
	}
	
	private void travelerDetailsHeader(PdfPTable table, Document document, Order order) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(20);
		table.setWidths(new float[] {3.0f, 11.5f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setSize(10);
		font.setColor(Color.BLACK);
		
		//1st cell................................................
		PdfPCell cell = new PdfPCell();
		travellerHeaderPart(cell);
		
		Chunk tvlr = new Chunk("Traveller Details: ");
		
		Phrase tvlrPhr = new Phrase();
		tvlrPhr.add(tvlr);
		
		cell.setPhrase(tvlrPhr);
		table.addCell(cell);
		
		//2nd cell................................................
		cell = new PdfPCell();
		travellerHeaderPart(cell);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

		ProductDetail flight = order.getProductDetail();
		
		Chunk baggageChk = new Chunk("Baggage info Check-In: " + flight.getBaggage() + ", Cabin: " + flight.getCabinBaggage() + " | LowFare");
		
		Phrase baggagePhr = new Phrase();
		baggagePhr.add(baggageChk);
		
		cell.setPhrase(baggagePhr);
		table.addCell(cell);
		
		document.add(table);
	}

	private void travellerHeaderPart(PdfPCell cell) {
		Color color = new Color(236, 237, 136);
		cell.setBorderColor(color);
		cell.setBackgroundColor(color);
		cell.setFixedHeight(20.0f);
	}
	
	private void travelerTableHeader(PdfPTable table, Document document, Order order) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(2);
		table.setWidths(new float[] {3.5f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(8);
		font.setColor(Color.BLACK);
		
		Rectangle border = new Rectangle(0f, 0f);
		Color color = new Color(175, 216, 247);
		Color color2 = new Color(101, 127, 197);
		
		//1st cell.........................................................
		PdfPCell cell = new PdfPCell();
		travellerTableHeaderPart(cell);
		
		border.setBackgroundColor(color);
		border.setBorderWidthTop(1f);
		border.setBorderWidthBottom(1f);
		border.setBorderWidthLeft(1f);
		border.setBorderColor(color2);
		cell.cloneNonPositionParameters(border);
		
		cell.setPhrase(new Phrase("\nTRAVELLER / (TYPE)", font));
		table.addCell(cell);
		
		//2nd cell.........................................................
		cell = new PdfPCell();
		travellerTableHeaderPart(cell);
		
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		
		cell.setPhrase(new Phrase("\nTICKET NUMBER", font));
		table.addCell(cell);
		
		//3rd cell.........................................................
		cell = new PdfPCell();
		travellerTableHeaderPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		cell.setPhrase(new Phrase("\nBASIC", font));
		table.addCell(cell);
		
		//4th cell.........................................................
		cell = new PdfPCell();
		travellerTableHeaderPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		cell.setPhrase(new Phrase("\nTAX", font));
		table.addCell(cell);
		
		//5th cell.........................................................
		cell = new PdfPCell();
		travellerTableHeaderPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		cell.setPhrase(new Phrase("\nCABIN + CHECK-IN", font));
		table.addCell(cell);
		
		//6th cell.........................................................
		cell = new PdfPCell();
		travellerTableHeaderPart(cell);
		border = new Rectangle(0f, 0f);

		border.setBackgroundColor(color);
		border.setBorderWidthTop(1f);
		border.setBorderWidthBottom(1f);
		border.setBorderWidthRight(1f);
		border.setBorderColor(color2);
		cell.cloneNonPositionParameters(border);
		
		cell.setPhrase(new Phrase("\nTOTAL FARE", font));
		table.addCell(cell);
		
		document.add(table);
	}

	private void travellerTableHeaderPart2(Rectangle border, PdfPCell cell, Color color, Color color2) {
		border.setBackgroundColor(color);
		border.setBorderWidthTop(1f);
		border.setBorderWidthBottom(1f);
		border.setBorderColor(color2);
		cell.cloneNonPositionParameters(border);
	}
	
	private void travellerTableHeaderPart(PdfPCell cell) {
		cell.setFixedHeight(30.0f);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	}
	
	private void travelerTableBody(PdfPTable table, Document document, Order order, TravellerDetail travellerDetail, ProductDetail productDetail) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(2);
		table.setWidths(new float[] {3.5f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(8);
		font.setColor(Color.BLACK);
		
		Rectangle border = new Rectangle(0f, 0f);
		Color color = new Color(223, 223, 223);
		Color color2 = new Color(101, 127, 197);
		
		//1st cell.........................................................
		PdfPCell cell = new PdfPCell();
		travellerTableBodyPart(cell);

		border.setBackgroundColor(color);
		border.setBorderWidthTop(1f);
		border.setBorderWidthBottom(1f);
		border.setBorderWidthLeft(1f);
		border.setBorderColor(color2);
		cell.cloneNonPositionParameters(border);
		
		cell.setPhrase(new Phrase(travellerDetail.getFirstName() + " " + travellerDetail.getLastName(), font));
		table.addCell(cell);
		
		//2nd cell.........................................................
		cell = new PdfPCell();
		travellerTableBodyPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		cell.setPhrase(new Phrase(productDetail.getPnr(), font));
		table.addCell(cell);
		
		//3rd cell.........................................................
		cell = new PdfPCell();
		travellerTableBodyPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		String price = "";
		if (travellerDetail.getPaxType().equals("3")) {
			price = Double.toString(productDetail.getPriceINF());
		} else {
			price = Double.toString(productDetail.getPriceADT());
		}
		 
		cell.setPhrase(new Phrase(price, font));
		table.addCell(cell);
		
		//4th cell.........................................................
		cell = new PdfPCell();
		travellerTableBodyPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);
		cell.setPhrase(new Phrase("0", font));
		table.addCell(cell);
		
		//5th cell.........................................................
		cell = new PdfPCell();
		travellerTableBodyPart(cell);
		border = new Rectangle(0f, 0f);
		travellerTableHeaderPart2(border, cell, color, color2);

		Integer convertCabinBaggage = productDetail.getCabinBaggage();
		String cabinBaggage = convertCabinBaggage.toString();
		
		cell.setPhrase(new Phrase(cabinBaggage + "KG+1", font));
		table.addCell(cell);
		
		//6th cell.........................................................
		cell = new PdfPCell();
		travellerTableBodyPart(cell);
		border = new Rectangle(0f, 0f);

		border.setBackgroundColor(color);
		border.setBorderWidthTop(1f);
		border.setBorderWidthBottom(1f);
		border.setBorderWidthRight(1f);
		border.setBorderColor(color2);
		cell.cloneNonPositionParameters(border);
		cell.setPhrase(new Phrase(price, font));
		table.addCell(cell);
		
		document.add(table);
	}
	
	private void travellerTableBodyPart(PdfPCell cell) {
		Color color = new Color(223, 223, 223);
		cell.setBorderColor(Color.BLUE);
		cell.setBackgroundColor(color);
		cell.setPaddingTop(5);
		cell.setFixedHeight(20.0f);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
	}
	
	private void travelerBorder(PdfPTable table, Document document) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(2);
		table.setWidths(new float[] {14.5f});
		
		PdfPCell cell = new PdfPCell();
		Color color = new Color(223, 223, 223);
		Color color2 = new Color(101, 127, 197);
		
		cell.setBorderColor(color2);
		cell.setBackgroundColor(color);
		cell.setFixedHeight(20.0f);
		table.addCell(cell);
		
		document.add(table);
		
	}
	
	private void totalFareTable(PdfPTable table, Document document, Order order) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(2);
		table.setWidths(new float[] {7.5f, 5.0f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(10);
		font.setColor(Color.BLACK);
		
		Font fontBig = FontFactory.getFont(FontFactory.HELVETICA);
		fontBig.setSize(12);
		fontBig.setColor(Color.BLACK);
		
		Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA);
		fontSmall.setSize(8);
		fontSmall.setColor(Color.BLACK);
		
		Color color = new Color(175, 216, 247);
		
		//1st cell.........................................................
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		
		Chunk grossChk = new Chunk("Fare Summary\n", fontBig);
		String price = Double.toString(order.getPrice());
		Chunk grossChk1 = new Chunk("Thank you for making the payment of @ - " + price + " INR.", fontSmall);
		
		Phrase grossPhrs = new Phrase();
		grossPhrs.add(grossChk); grossPhrs.add(grossChk1);
		
		cell.setPhrase(grossPhrs);
		table.addCell(cell);
		
		//2nd cell.........................................................
		cell = new PdfPCell();
		cell.setBackgroundColor(color);
		cell.setBorderColor(Color.BLACK);
		cell.setPaddingTop(3);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		
		Chunk priceChk = new Chunk("GROSS FARE: @ - ", fontBig);
		Chunk priceChk1 = new Chunk(price, font);
		
		Phrase pricePhrs = new Phrase();
		pricePhrs.add(priceChk); pricePhrs.add(priceChk1);
		
		cell.setPhrase(pricePhrs);
		table.addCell(cell);
		
		document.add(table);
	}
	
	private void extraDetailsAfterPrice(PdfPTable table, Document document, Order order, String siteLogo) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(15);
		table.setWidths(new float[] {7.5f, 5.0f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(8);
		font.setColor(Color.BLACK);
		
		Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA);
		fontSmall.setSize(8);
		fontSmall.setColor(Color.BLACK);
		
		Rectangle border = new Rectangle(0f, 0f);
		border.setBorderWidthBottom(1);
		border.setBorderColorBottom(Color.BLACK);
		
		//1st cell.........................................................
		PdfPCell cell = new PdfPCell();
		Path path = Paths.get(".." + siteLogo);
		Image imgSiteLogo = Image.getInstance(path.toFile().getAbsolutePath());
		imgSiteLogo.scaleAbsolute(70, 25);
		
		Chunk extraLogoChk =  new Chunk(imgSiteLogo, 0, 0);
		
		Phrase extraLogoPhrs = new Phrase();
		extraLogoPhrs.add(extraLogoChk);
		
		cell.setPhrase(extraLogoPhrs);
		cell.cloneNonPositionParameters(border);
		table.addCell(cell);
		
		//2nd cell.........................................................
		cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		
		Date date = order.getCreatedTime();  
	    DateFormat dateFormat3 = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);  
	    String strDate = "Issued Date: " + dateFormat3.format(date); 
	    
	    Chunk extraChk = new Chunk("Booking Ref: \n", fontSmall);
		Chunk extraChk1 = new Chunk(strDate, fontSmall);
		
		Phrase extraPhrs = new Phrase();
		extraPhrs.add(extraChk); extraPhrs.add(extraChk1);
		
		cell.setPhrase(extraPhrs);
		cell.cloneNonPositionParameters(border);
		table.addCell(cell);
		
		document.add(table);
	}
	
	private void travellerInfo(PdfContentByte cb, PdfPTable table, Document document, Order order) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(4);
		table.setWidths(new float[] {7.5f, 1.5f, 3.5f});
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(9);
		font.setColor(Color.BLACK);
		
		Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA);
		fontSmall.setSize(9);
		fontSmall.setColor(Color.BLACK);
		
		//1st cell.........................................................
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		
		Chunk infoChk = new Chunk("Traveller Information\n", font);
		String phNum = order.getPhoneNumber().toString();
		Chunk infoChk1 = new Chunk("Contact No.: " + phNum + "\n", fontSmall);
		Chunk infoChk2 = new Chunk("Email: " + order.getContactEmail(), fontSmall);
		
		Phrase infoPhrs = new Phrase();
		infoPhrs.add(infoChk); infoPhrs.add(infoChk1); infoPhrs.add(infoChk2);
		
		cell.setPhrase(infoPhrs);
		table.addCell(cell);
		
		//2nd cell.........................................................
		cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		table.addCell(cell);
		
		//3rd cell.........................................................
		cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingBottom(5);
		ProductDetail flight = order.getProductDetail();
		String pnr = flight.getPnr();
		
		Barcode39 code39 = new Barcode39();
		code39.setCode(pnr.toUpperCase());
        code39.setStartStopText(false);
        Image image39 = code39.createImageWithBarcode(cb, null, null);
        
        cell.setPhrase(new Phrase(new Chunk(image39, 0, 0)));
        table.addCell(cell);
		
		document.add(table);
	}
	
	private void helpInfo(PdfPTable table, Document document) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setWidths(new float[] {14.5f});
		
		Color color = new Color(20, 30, 120);
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(11);
		font.setColor(color);
		
		Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA);
		fontSmall.setSize(11);
		fontSmall.setColor(color);
		
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		cell.setPaddingBottom(10);
		Chunk bold = new Chunk(" \s\sNeed Help with your trip?\r\n", font);
		Chunk normal = new Chunk(" \s\sContact - EASEGOFLY TEAM \r\n \s\s8348000139, support@easegofly.com", fontSmall);
		Chunk normal1 = new Chunk(" \s\sFlight web checkin", font);
		
		Phrase p = new Phrase();
		p.add(bold);
		p.add(normal);
		p.add(normal1);
		
		cell.setPhrase(p);
		table.addCell(cell);
		
		document.add(table);
	}
	
	private void additionalInfo(PdfPTable table, Document document) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setWidths(new float[] {14.5f});
		
		Color color = new Color(20, 30, 120);
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(12);
		font.setColor(color);
		
		Font fontSmall = FontFactory.getFont(FontFactory.TIMES_ROMAN);
		fontSmall.setSize(12);
		fontSmall.setColor(Color.BLACK);
		
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		Chunk bold = new Chunk("ImportantInstructions and Fare Rules for Travelers: \r\n", font);
		Chunk normal = new Chunk("1. Please carry a Valid Photo Identity Proof.\r\n", fontSmall);
		Chunk normal1 = new Chunk("2. Check-in counter closes strictly 45-mins prior departure time. Please Check-in atleast 2.5 Hrs prior departure"
				+ "time.\r\n", fontSmall);
		Chunk normal2 = new Chunk("3. Delay & Cancellation of Flights are out of our or airline’s control. Please get in touch with Airline Staffs for"
				+ "alternate arrangements or alternate date on same airline. Please provide active & correct contact numbers to keep"
				+ "you updated about flight schedules. Agency or Airline shall not be responsible for any inconvenience if you are"
				+ "unreachable or have provided incorrect mobile number.\r\n", fontSmall);
		Chunk normal3 = new Chunk("4. This is Group Fare Booking. Non-Cancellable and Non-Changeable.\r\n", fontSmall);
		Chunk normal4 = new Chunk("5. Fare once booked, cannot be discounted further even if system fare reduces & is an agreement between buyerseller.\r\n", fontSmall);
		
		Phrase p = new Phrase();
		p.add(bold); p.add(normal); p.add(normal1); p.add(normal2); p.add(normal3); p.add(normal4);
		cell.setFixedHeight(140.0f);
		cell.setPhrase(p);
		
		table.addCell(cell);
		table.completeRow();
		
		document.add(table);
	}
	
	private void footerDialog(PdfPTable table, Document document) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(15);
		table.setWidths(new float[] {14.5f});
		
		Color color = new Color(20, 30, 120);
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(8);
		font.setColor(color);
		
		Font font1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font1.setSize(12);
		font1.setColor(Color.RED);
		
		Font fontRed = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		fontRed.setSize(12);
		fontRed.setColor(color);
		
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(Color.WHITE);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		
		Chunk bold = new Chunk("Naws Tour & Travels is not liable for any Discrepancy / Deficiency in service by the Airline or Service Providers.. Any discrepancy regarding this ticket,"
				+ "please inform us within 3 hrs of Issuance. After that we are not liable for any changes.\r\n", font);
		Chunk bold1 = new Chunk("!!! Have a Nice Trip !!\r\n", font);
		Chunk bold2 = new Chunk("\nThanks For Booking With ", font1);
		Chunk bold3 = new Chunk("NAWS TOUR AND TRAVELS", fontRed);
		Phrase p = new Phrase();
		p.add(bold); p.add(bold1); p.add(bold2); p.add(bold3);
		cell.setPhrase(p);
		
		table.addCell(cell);
		table.completeRow();
		
		document.add(table);
	}
	
	private void footerInfo(PdfPTable table, Document document) throws BadElementException, IOException {
		table.setWidthPercentage(100f);
		table.setSpacingBefore(15);
		table.setWidths(new float[] {12.5f, 2.0f});
		
		Color color = new Color(223, 223, 223);
		Color color1 = new Color(170, 180, 190);
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(8);
		font.setColor(Color.BLACK);
		
		//1st cell.........................................................
		PdfPCell cell = new PdfPCell();
		cell.setBorderColor(color1);
		cell.setBackgroundColor(color);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		
		Chunk text = new Chunk("+ BAGGAGE DISCOUNT MAY APPLY BASSED ON FREQUEST FLYER STATUS/ONLINE CHECKIN/FORM OF PAYMENT/MILITARY/ETC.", font);
		Chunk text1 = new Chunk("Email: support@easegofly.com \s\sMobile: +91-8348000139 \s\sWhatsapp: +91-9732504064", font);
		
		Phrase p = new Phrase();
		p.add(text); p.add(text1);
		cell.setPhrase(p);
		table.addCell(cell);
		
		//1st cell.........................................................
		cell = new PdfPCell();
		cell.setPadding(0);
		Path path = Paths.get("../pdf-images/thumb-logo.png");
		Image thumbLogo = Image.getInstance(path.toFile().getAbsolutePath());
		thumbLogo.scaleAbsolute(73, 22);
		
		Chunk dp6 = new Chunk(thumbLogo, 0, 0);
		Phrase img = new Phrase();
		img.add(dp6);
		cell.setPhrase(img);
		table.addCell(cell);
		
		document.add(table);
	}
}
