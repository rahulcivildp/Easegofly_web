package com.easygofly.admin.customer.export;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.easygofly.entity.Customer;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class UserPDFExporter extends AbstractExporter {

	public void export(List<Customer> listCust, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "application/pdf", ".pdf");
		
		Document document = new Document(PageSize.A4);
		PdfWriter.getInstance(document, response.getOutputStream());
		
		document.open();
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(18);
		font.setColor(Color.BLUE);
		
		Paragraph paragraph = new Paragraph("List of Customer", font);
		paragraph.setAlignment(Paragraph.ALIGN_CENTER);
		
		document.add(paragraph);
		
		PdfPTable table = new PdfPTable(6);
		table.setWidthPercentage(100f);
		table.setSpacingBefore(10);
		table.setWidths(new float[] {1.0f, 4.0f, 3.0f, 3.0f, 3.3f, 1.2f});
		
		writeTableHeader(table);
		writeTableData(table, listCust);
		
		document.add(table);
		
		document.close();
		
	}

	private void writeTableData(PdfPTable table, List<Customer> listCust) {
		for (Customer cust : listCust) {
			table.addCell(String.valueOf(cust.getId()));
			table.addCell(cust.getEmail());
			table.addCell(cust.getPhone());
			table.addCell(cust.getFirstName());
			table.addCell(cust.getLastName());
			table.addCell(String.valueOf(cust.isEnabled()));
		}
		
	}

	private void writeTableHeader(PdfPTable table) {
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.BLUE);
		cell.setPadding(5);
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA);
		font.setColor(Color.WHITE);
		
		cell.setPhrase(new Phrase("ID", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Email", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Phone", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("First Name", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Last Name", font));
		table.addCell(cell);
		
		cell.setPhrase(new Phrase("Enabled", font));
		table.addCell(cell);
		
	}
}
