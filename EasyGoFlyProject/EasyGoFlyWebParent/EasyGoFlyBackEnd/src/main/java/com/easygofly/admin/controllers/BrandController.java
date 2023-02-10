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
import com.easygofly.admin.brand.BrandService;
import com.easygofly.admin.category.CategoryService;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;
import com.easygofly.entity.exception.BrandNotFoundException;


@Controller
public class BrandController {

	@Autowired
	public BrandService brandService;
	
	@Autowired
	public CategoryService categoryService;
	
	@GetMapping("/brands")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "name", "asc", null);
		
	}
	
	@GetMapping("/brands/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		//System.out.println("Sort Field: " + sortField);
		//System.out.println("Sort Order: " + sortDir);
		Page<Brand> pageBrand = brandService.listByPage(pageNum, sortField, sortDir, keyword);
		Page<Category> pageCategory = categoryService.listByPage(pageNum, sortField, sortDir, keyword);
		
		List<Category> listCategoriesInTable = categoryService.listAllHierarchicalCategories();
		List<Category> listCategories = pageCategory.getContent();
		List<Brand> listBrands = pageBrand.getContent();
		
		long startCount = (pageNum - 1) * BrandService.BRAND_PER_PAGE + 1;
		long endCount = startCount + BrandService.BRAND_PER_PAGE - 1;
		if (endCount > pageBrand.getTotalElements()) {
			endCount = pageBrand.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageBrand.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageBrand.getTotalElements());
		model.addAttribute("listBrands", listBrands);
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("listCategoriesInTable", listCategoriesInTable);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		
		return "brands/brands";
	}
	
	@GetMapping("/brands/{id}/enabled/{status}")
	public String updateUnabledStatus(@PathVariable(name = "id") Integer id, @PathVariable(name = "status") boolean enabled, Model model, RedirectAttributes redirectAttributes) {
		brandService.updateUserEnabledStatus(id, enabled);
		String status = enabled? "ENABLED" : "DISABLED";
		redirectAttributes.addFlashAttribute("message", "Brand ID: " + id + " is successfully " + status + ".");
		return "redirect:/brands";
	}
	
	@GetMapping("/brands/new")
	public String newUser(Model model) {
		List<Category> listCategories = categoryService.listCategoriesUsedInForm();
		
		Brand brand = new Brand();
		brand.setEnabled(true);
		
		model.addAttribute("brand", brand);
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("pageTitle", "Create new brand");
		
		return "brands/brand_form";
	}
	
	@GetMapping("/brands/edit/{id}")
	public String editBrand(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Brand brand = brandService.updateBrand(id);
			List<Category> listCategories = brandService.listCategories();
			
			model.addAttribute("brand", brand);
			model.addAttribute("pageTitle", "Edit Brand: ID - " + id);
			model.addAttribute("listCategories", listCategories);
			
			return "brands/brand_form";
		} catch (BrandNotFoundException e) {
			redirectAttributes.addFlashAttribute("alert", e.getMessage());
			return "redirect:/brands";
		}
	}
	
	@PostMapping("/brands/save")
	public String saveCategory(Brand brand, RedirectAttributes redirectAttributes, @RequestParam("fileImage") MultipartFile multipartFile) throws IOException {
		
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			brand.setLogo(fileName);
			Brand savedCategory = brandService.saveBrand(brand);
			String uploadDir = "../brand-logos/" + savedCategory.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}else {
			if(brand.getLogo().isEmpty()) {
				brand.setLogo(null);
			}
			brandService.saveBrand(brand);
		}
		
		
		redirectAttributes.addFlashAttribute("message", "The brand item has been saved sucessfully.");
		return getRedirectURLtoAffectedCategory(brand);
	}
	
	private String getRedirectURLtoAffectedCategory(Brand brand) {
		String brandName = brand.getName();
		return "redirect:/brands/page/1?sortField=id&sortDir=asc&keyword=" + brandName;
	}
	
	@GetMapping("/brands/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			brandService.deleteUser(id);
			redirectAttributes.addFlashAttribute("warning", "User ID: " + id + " is deleted successfully.");
		} catch (BrandNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/brands";
	}
}
