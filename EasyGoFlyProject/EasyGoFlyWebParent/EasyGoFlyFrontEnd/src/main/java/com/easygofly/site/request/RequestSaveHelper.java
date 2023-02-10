package com.easygofly.site.request;

import java.util.Date;

import com.easygofly.entity.Request;

public class RequestSaveHelper {

	public static void setConversation(Request request, String repliedFrom, String chatBody, Date createdTime) {
		request.addConversation(repliedFrom, chatBody, createdTime);
	}
}
