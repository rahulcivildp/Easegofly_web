package com.easygofly.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "requests")
public class Request {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 64, nullable = false)
	private String subject;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;
	
	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
	private List<Conversation> conversations = new ArrayList<>();
	
	
	public Request() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public List<Conversation> getConversations() {
		return conversations;
	}

	public void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}
	
	public void addConversation(String repliedFrom, String chatBody, Date createdTime) {
		this.conversations.add(new Conversation(repliedFrom, chatBody, createdTime, this));
	}
	
	public void addConversation(int id, String repliedFrom, String chatBody, Date createdTime) {
		this.conversations.add(new Conversation(id, repliedFrom, chatBody, createdTime, this));
	}
	
}
