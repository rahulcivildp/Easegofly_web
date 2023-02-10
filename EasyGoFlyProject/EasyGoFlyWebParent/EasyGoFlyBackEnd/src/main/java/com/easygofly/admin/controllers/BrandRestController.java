package com.easygofly.admin.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygofly.admin.brand.BrandService;
import com.easygofly.admin.category.CategoryDTO;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;


@RestController
public class BrandRestController {

	@Autowired
	private BrandService service;
	
	@PostMapping("/brands/check_name")
	public String chechDuplicateName(@Param("id") Integer id, @Param("name") String name) {
		return service.isNameUnique(id, name) ? "OK" : "Duplicate Name";
	}
	
	@GetMapping("/brands/{id}/categories")
	public List<CategoryDTO> listCategoriesByBrand(@PathVariable(name = "id") Integer brandId) {
		List<CategoryDTO> listCategories = new ArrayList<>();
		try {
			Brand brand = service.updateBrand(brandId);
			Set<Category> categories = brand.getCategories();
			for (Category category : categories) {
				CategoryDTO dto = new CategoryDTO(category.getId(), category.getName());
				listCategories.add(dto);
			}
			return listCategories;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
