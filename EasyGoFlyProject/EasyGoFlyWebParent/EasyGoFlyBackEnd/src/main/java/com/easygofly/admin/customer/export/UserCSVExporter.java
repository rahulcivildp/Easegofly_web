package com.easygofly.admin.customer.export;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.easygofly.entity.Customer;


public class UserCSVExporter extends AbstractExporter {

	public void export(List<Customer> listCust, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv", ".csv");
		
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		
		String[] csvHeader = {"ID", "Email", "Phone", "First Name", "Last Name",  "Enabled"};
		String[] fieldMapping = {"id", "email", "phone", "firstName", "lastName", "enabled"};
		
		csvWriter.writeHeader(csvHeader);
		for (Customer cust : listCust) {
			csvWriter.write(cust, fieldMapping);
		}
		
		csvWriter.close();
	}
}
