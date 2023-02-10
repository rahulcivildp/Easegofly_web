package com.easygofly.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "conversations")
public class Conversation {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 128)
	private String repliedFrom; 
	
	@Column(length = 10240)
	private String chatBody;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@ManyToOne
	@JoinColumn(name = "request_id")
	private Request request;
	
	

	public Conversation() {}

	public Conversation(String repliedFrom, String chatBody, Date createdTime, Request request) {
		this.repliedFrom = repliedFrom;
		this.chatBody = chatBody;
		this.createdTime = createdTime;
		this.request = request;
	}

	public Conversation(Integer id, String repliedFrom, String chatBody, Date createdTime, Request request) {
		this.id = id;
		this.repliedFrom = repliedFrom;
		this.chatBody = chatBody;
		this.createdTime = createdTime;
		this.request = request;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRepliedFrom() {
		return repliedFrom;
	}

	public void setRepliedFrom(String repliedFrom) {
		this.repliedFrom = repliedFrom;
	}

	public String getChatBody() {
		return chatBody;
	}

	public void setChatBody(String chatBody) {
		this.chatBody = chatBody;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	
	
	
}
