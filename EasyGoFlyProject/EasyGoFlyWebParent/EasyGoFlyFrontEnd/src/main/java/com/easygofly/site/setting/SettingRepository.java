package com.easygofly.site.setting;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.easygofly.entity.Setting;
import com.easygofly.entity.SettingCategory;


public interface SettingRepository extends CrudRepository<Setting, String> {
	
	public List<Setting> findByCategory(SettingCategory category);

	@Query("SELECT s FROM Setting s WHERE s.category = ?1 OR s.category = ?2")
	public List<Setting> findByTwoCategories(SettingCategory catOne, SettingCategory catTwo);
}
