package com.easygofly.api.web;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.WebDetails;
import com.easygofly.entity.WebSettingCategory;

public interface WebSettingRepository extends CrudRepository<WebDetails, String> {
	
	public List<WebDetails> findByCategory(WebSettingCategory category);
	
	@Query("SELECT w FROM WebDetails w WHERE w.key = :key")
	public WebDetails findByKey(String key);
	
	@Query("SELECT s FROM WebDetails s WHERE s.category = ?1 OR s.category = ?2 OR s.category = ?3 OR s.category = ?4 ")
	public List<WebDetails> findByAllCategories(WebSettingCategory catOne, WebSettingCategory catTwo, WebSettingCategory catThree, WebSettingCategory catfour);
}
