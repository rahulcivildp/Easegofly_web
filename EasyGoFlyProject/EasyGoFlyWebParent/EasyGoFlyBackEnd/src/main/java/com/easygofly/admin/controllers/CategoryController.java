package com.easygofly.admin.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.FileUploadUtil;
import com.easygofly.admin.category.CategoryService;
import com.easygofly.entity.Category;
import com.easygofly.entity.exception.CategoryNotFoundException;


@Controller
public class CategoryController {
	
	@Autowired
	private CategoryService service;
	
	@GetMapping("/categories")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "name", "asc", null);
		
	}
	
	@GetMapping("/categories/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		//System.out.println("Sort Field: " + sortField);
		//System.out.println("Sort Order: " + sortDir);
		Page<Category> pageCategory = service.listByPage(pageNum, sortField, sortDir, keyword);
		
		List<Category> listCategoriesInTable = service.listCategoriesUsedInForm();
		List<Category> listCategories = pageCategory.getContent();
		
		long startCount = (pageNum - 1) * CategoryService.ITEMS_PER_PAGE + 1;
		long endCount = startCount + CategoryService.ITEMS_PER_PAGE - 1;
		if (endCount > pageCategory.getTotalElements()) {
			endCount = pageCategory.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageCategory.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageCategory.getTotalElements());
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		model.addAttribute("listCategoriesInTable", listCategoriesInTable);
		
		return "categories/categories";
	}
	
	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateUnabledStatus(@PathVariable(name = "id") Integer id, @PathVariable(name = "status") boolean enabled, Model model, RedirectAttributes redirectAttributes) {
		service.updateCategoryEnabledStatus(id, enabled);
		String status = enabled? "ENABLED" : "DISABLED";
		redirectAttributes.addFlashAttribute("message", "Category: " + id + " is successfully " + status + ".");
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		List<Category> listCategories = service.listCategoriesUsedInForm();
		
		Category category = new Category();
		category.setEnabled(true);
		
		model.addAttribute("category", category);
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("pageTitle", "Create new category");
		
		return "categories/category_form";
	}
	
	@PostMapping("/categories/save")
	public String saveCategory(Category category, RedirectAttributes redirectAttributes, @RequestParam("fileImage") MultipartFile multipartFile) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);
			Category savedCategory = service.saveCategory(category);
			
			System.out.println(category.getId());
			
			String uploadDir = "../category-photos/" + savedCategory.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if(category.getImage().isEmpty()) {
				category.setImage(null);
			}
			service.saveCategory(category);
		}
		
		
		redirectAttributes.addFlashAttribute("message", "The user has been saved sucessfully.");
		return getRedirectURLtoAffectedCategory(category);
	}

	private String getRedirectURLtoAffectedCategory(Category category) {
		String CategoryName = category.getName();
		return "redirect:/categories/page/1?sortField=id&sortDir=asc&keyword=" + CategoryName;
	}
	
	@GetMapping("/categories/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Category category = service.updateCategory(id);
			List<Category> listCategories = service.listCategoriesUsedInForm();
			
			model.addAttribute("category", category);
			model.addAttribute("pageTitle", "Edit Category: ID - " + id);
			model.addAttribute("listCategories", listCategories);
			
			return "categories/category_form";
		} catch (CategoryNotFoundException e) {
			redirectAttributes.addFlashAttribute("alert", e.getMessage());
			return "redirect:/categories";
		}
	}
	
	@GetMapping("/categories/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			service.deleteCategory(id);
			redirectAttributes.addFlashAttribute("warning", "Category ID: " + id + " is deleted successfully.");
		} catch (CategoryNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/categories";
	}
	
	@PostMapping("/categories/check_name")
	public String chechDuplicateEmail(@Param("id") Integer id, @Param("name") String name) {
		return service.isNameUnique(id, name) ? "OK" : "Duplicate Name";
	}
}
