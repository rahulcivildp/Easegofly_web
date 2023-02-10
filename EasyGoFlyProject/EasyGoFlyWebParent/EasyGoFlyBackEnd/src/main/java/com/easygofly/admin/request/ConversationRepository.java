package com.easygofly.admin.request;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Conversation;
import com.easygofly.entity.Request;

public interface ConversationRepository extends CrudRepository<Conversation, Integer> {
	
	@Query("SELECT c FROM Conversation c WHERE c.request= ?1 ")
	public List<Conversation> findConversationByRequest(Request request);
}
