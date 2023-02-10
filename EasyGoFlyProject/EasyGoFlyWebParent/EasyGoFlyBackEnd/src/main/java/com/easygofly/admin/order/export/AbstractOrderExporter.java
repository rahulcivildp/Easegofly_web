package com.easygofly.admin.order.export;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public class AbstractOrderExporter {

	public void setResponseHeader(HttpServletResponse response, String contentType, String extension) throws IOException {
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timeStamp = dateFormatter.format(new Date());
		String fileName = "Easegofly_eTicket-" + timeStamp + extension;
		
		response.setContentType(contentType);
		
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; fileName=" + fileName;
		
		response.setHeader(headerKey, headerValue);
		
		
	}
}
