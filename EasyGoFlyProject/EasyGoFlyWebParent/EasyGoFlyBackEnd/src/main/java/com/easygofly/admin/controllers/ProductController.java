package com.easygofly.admin.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easygofly.admin.category.CategoryService;
import com.easygofly.admin.product.ProductDetailRepository;
import com.easygofly.admin.product.ProductDetailService;
import com.easygofly.admin.product.ProductSaveHelper;
import com.easygofly.admin.product.ProductService;
import com.easygofly.admin.security.EasyGoFlyUserDetails;
import com.easygofly.admin.setting.city.CityRepository;
import com.easygofly.admin.user.UserRepository;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;
import com.easygofly.entity.City;
import com.easygofly.entity.Product;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.User;
import com.easygofly.entity.exception.CategoryNotFoundException;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.entity.exception.UserNotFoundException;


@Controller
public class ProductController {
	
	@Autowired private ProductService productService;
	@Autowired private CategoryService categoryService;
	@Autowired private CityRepository cityRepo;
	@Autowired private ProductDetailService productDetailService;
	@Autowired private ProductDetailRepository productDetailRepo;
	@Autowired private UserRepository userRepo;
	
	@GetMapping("/products")
	public String listFirstPage(Model model, @AuthenticationPrincipal EasyGoFlyUserDetails loggedUser) {
		String product = "products/products";
		
		return listByPage(1, model, "name", "asc", null, 0, product, loggedUser);
	}
	
	@GetMapping("/products/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, 
			@Param("sortField") String sortField, 
			@Param("sortDir") String sortDir, 
			@Param("keyword") String keyword, 
			@Param("categoryId") Integer categoryId, 
			String formLink, @AuthenticationPrincipal EasyGoFlyUserDetails loggedUser
			) {
		User user = userRepo.getUserByEmail(loggedUser.getUsername());
		System.out.println("Selected Category ID: " + user.getEmail());
		//System.out.println("Sort Field: " + sortField);
		//System.out.println("Sort Order: " + sortDir);
		Page<Product> productPage = productService.listByPageUser(pageNum, sortField, sortDir, keyword, categoryId, user);
		Page<Product> productPageAdmin = productService.listByPage(pageNum, sortField, sortDir, keyword, categoryId);
		List<Product> listProducts = productPage.getContent();
		List<Product> listProductsAdmin = productPageAdmin.getContent();
		List<Product> listAll = productService.listAll();
		
		List<Category> listCategories = categoryService.listCategoriesUsedInForm();
		
		long startCount = (pageNum - 1) * CategoryService.ITEMS_PER_PAGE + 1;
		long endCount = startCount + CategoryService.ITEMS_PER_PAGE - 1;
		if (endCount > productPageAdmin.getTotalElements()) {
			endCount = productPageAdmin.getTotalElements();
		}
		
		long startCount2 = (pageNum - 1) * CategoryService.ITEMS_PER_PAGE + 1;
		long endCount2 = startCount2 + CategoryService.ITEMS_PER_PAGE - 1;
		if (endCount2 > productPage.getTotalElements()) {
			endCount2 = productPage.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		if(categoryId != null) {
			model.addAttribute("categoryId", categoryId);
		}
		
		List<User> users = (List<User>) userRepo.findAll();

		model.addAttribute("users", users);
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", productPageAdmin.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", productPageAdmin.getTotalElements());
		model.addAttribute("totalPages2", productPage.getTotalPages());
		model.addAttribute("startCount2", startCount2);
		model.addAttribute("endCount2", endCount2);
		model.addAttribute("totalItems2", productPage.getTotalElements());
		model.addAttribute("listProducts", listProducts);
		model.addAttribute("listProductsAdmin", listProductsAdmin);
		model.addAttribute("listAll", listAll);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);
		model.addAttribute("listCategories", listCategories);
		
		List<Category> listCategory = categoryService.listCategoriesUsedInForm();
		List<Brand> listBrand = productService.listBrand();
		List<City> cities = cityRepo.findAllByOrderByNameAsc();
		
		Product product = new Product();
		product.setEnabled(true);
		product.setInStock(true);
		product.setFundState(true);
		
		model.addAttribute("product", product);
		model.addAttribute("listCategory", listCategory);
		model.addAttribute("listBrand", listBrand);
		model.addAttribute("cities", cities);
		model.addAttribute("pageTitle", "Create new product");
		
		return formLink;
	}
	
	@GetMapping("/products/{id}/enabled/{status}")
	public String updateUnabledStatus(@PathVariable(name = "id") Integer id, @PathVariable(name = "status") boolean enabled, Model model, RedirectAttributes redirectAttributes) {
		productService.updateUserEnabledStatus(id, enabled);
		String status = enabled? "ENABLED" : "DISABLED";
		redirectAttributes.addFlashAttribute("message", "Product ID: " + id + " is successfully " + status + ".");
		return "redirect:/products";
	}
	
	@PostMapping("/products/save")
	public String saveProduct(Product products, RedirectAttributes redirectAttributes,  
			@AuthenticationPrincipal EasyGoFlyUserDetails loggedUser) throws IOException, ProductNotFoundException {
		
		User user = userRepo.getUserByEmail(loggedUser.getUsername());
		if (loggedUser.hasRole("Salesperson")) {
			Product product = productService.getProductDetails(products.getId());
			productService.saveProductPrice(products);
			redirectAttributes.addFlashAttribute("message", "The product price has been saved sucessfully.");
			return ProductSaveHelper.getRedirectURLtoAffectedUser(product);
		}
		
		productService.saveProduct(products, user);
		
		redirectAttributes.addFlashAttribute("message", "The product has been saved sucessfully.");
		
		return "redirect:/products";
	}
	
	@PostMapping("/products/user-save")
	public String saveProductByUser(Product products, RedirectAttributes redirectAttributes,  
			@AuthenticationPrincipal EasyGoFlyUserDetails loggedUser, @RequestParam(name = "email") String email) throws IOException, ProductNotFoundException {
		
		User user = userRepo.getUserByEmail(email);
		if (loggedUser.hasRole("Salesperson")) {
			Product product = productService.getProductDetails(products.getId());
			productService.saveProductPrice(products);
			redirectAttributes.addFlashAttribute("message", "The product price has been saved sucessfully.");
			return ProductSaveHelper.getRedirectURLtoAffectedUser(product);
		}
		
		productService.saveProduct(products, user);
		
		redirectAttributes.addFlashAttribute("message", "The product has been saved sucessfully.");
		
		return "redirect:/products";
	}

	@GetMapping("/products/flights/{id}")
	public String listFlighPage(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal EasyGoFlyUserDetails loggedUser) throws ProductNotFoundException {
		try{
			Product productDetails = productService.getProductDetails(id);
			
			model.addAttribute("productDetails", productDetails);
			String flights = "products/flights";
			
		return listByPage(1, model, "name", "asc", null, 0, flights, loggedUser);
		} catch (ProductNotFoundException e) {
			redirectAttributes.addFlashAttribute("alert", e.getMessage());
			return "redirect:/products";
		}
	}
	
	@PostMapping("/products/flights/save")
	public String listFlighSave(Product products, RedirectAttributes redirectAttributes,  
			@RequestParam(name = "pnr", required = false) String pnr, 
			@RequestParam(name = "totalSeats", required = false) String totalSeats,
			@RequestParam(name = "uploadSeats", required = false) String uploadSeats, 
			@RequestParam(name = "flightNum", required = false) String flightNum, 
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date,
			@RequestParam(name = "depTime", required = false) String depTime, 
			@RequestParam(name = "arrTime", required = false) String arrTime, 
			@RequestParam(name = "priceADT", required = false) float priceADT,
			@RequestParam(name = "priceINF", required = false) float priceINF,
			@RequestParam(name = "markupADT", required = false) float markupADT, 
			@RequestParam(name = "markupINF", required = false) float markupINF, 
			@RequestParam(name = "stops", required = false) int stops,  
			@RequestParam(name = "duration", required = false) Integer duration, 
			@AuthenticationPrincipal EasyGoFlyUserDetails loggedUser) throws IOException, ProductNotFoundException {
		
		System.out.println("arrTime" + arrTime);
		
		Product product = productService.getProductDetails(products.getId());
		String cityOne = product.getCityOne();
		String cityTwo = product.getCityTwo();
		boolean setEnable = true;
		boolean setInStock = true;
		Brand brand = product.getBrands();
		
		ProductSaveHelper.setProductDetails(products, pnr, totalSeats, uploadSeats, flightNum, date, depTime, arrTime, priceADT, 
				priceINF, markupADT, markupINF, cityOne, cityTwo, setInStock, setEnable, stops, duration, brand.getName());
		
		productService.saveProductDetails(products);
		
		redirectAttributes.addFlashAttribute("message", "Flight is saved sucessfully.");
		
		return "redirect:/products/flights/" + products.getId();
	}
	
	@PostMapping("/products/flights_details")
	public String createStops(@RequestParam(name = "product_id") Integer product_id, 
			@RequestParam(name = "flight_id") Integer flight_id,
			@RequestParam(name = "stopCityName", required = false) String[] cityName,
			@RequestParam(name = "stopDepTime", required = false) String[] depTime,
			@RequestParam(name = "stopArrTime", required = false) String[] arrTime, 
			@RequestParam(name = "stopTotalTime", required = false) String[] totalTime, 
			RedirectAttributes redirectAttributes) throws ProductNotFoundException {
		
			ProductDetail productDetail = productDetailService.getFlightDetails(flight_id);
			ProductSaveHelper.setStopDetails(cityName, depTime, arrTime, totalTime, productDetail);
			
			productDetailService.saveFlightDetails(productDetail);
			
			return "redirect:/products/flights/" + product_id;
	}
	
	@PostMapping("/products/flights_details_edit")
	public String editStops(@RequestParam(name = "product_id") Integer product_id, 
			@RequestParam(name = "flight_id") Integer flight_id,
			@RequestParam(name = "stop_id") Integer[] stop_id,
			@RequestParam(name = "stopCityNameEdit", required = false) String[] cityName,
			@RequestParam(name = "stopDepTimeEdit", required = false) String[] depTime,
			@RequestParam(name = "stopArrTimeEdit", required = false) String[] arrTime, 
			@RequestParam(name = "stopTotalTimeEdit", required = false) String[] totalTime, 
			RedirectAttributes redirectAttributes) throws ProductNotFoundException {
		
			System.out.println("products.getId(): " + product_id);
			System.out.println("productDetails.getId(): " + flight_id);
		
			ProductDetail productDetail = productDetailService.getFlightDetails(flight_id);
			ProductSaveHelper.editStopDetails(stop_id, cityName, depTime, arrTime, totalTime, productDetail);
			
			productDetailService.saveFlightDetails(productDetail);
			
			return "redirect:/products/flights/" + product_id;
	}
	
	@GetMapping("/products/edit/{id}")
	public String editProduct(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) throws ProductNotFoundException {
		try {
			Product product = productService.getProductDetails(id);
			List<Category> listCategory = categoryService.listCategoriesUsedInForm();
			List<Brand> listBrand = productService.listBrand();
			
			model.addAttribute("product", product);
			model.addAttribute("pageTitle", "Edit Product: ID - " + id);
			model.addAttribute("listCategory", listCategory);
			model.addAttribute("listBrand", listBrand);
			
			return "products/product_form";
		} catch (ProductNotFoundException e) { 
			redirectAttributes.addFlashAttribute("alert", e.getMessage());
			return "redirect:/products";
		}
	}
	
	@GetMapping("/products/detail/{id}")
	public String viewProductDetails(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) throws ProductNotFoundException {
		try {
			Product product = productService.getProductDetails(id);
			model.addAttribute("product", product);
			
			return "products/product_detail_modal";
		} catch (ProductNotFoundException e) {
			redirectAttributes.addFlashAttribute("alert", e.getMessage());
			return "redirect:/products";
		}
	}
	
	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			productService.deleteProduct(id);
			redirectAttributes.addFlashAttribute("warning", "Product ID: " + id + " is deleted successfully.");
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
		}
		return "redirect:/products";
	}
	
	@GetMapping("/product_details/delete/{id}")
	public String deleteProductDetails(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			ProductDetail deletedFlight = productDetailRepo.findById(id).get();
			Product product = deletedFlight.getProduct();
			if (deletedFlight.getOrder() == null) {
				productDetailService.deleteProductDetail(id);
				redirectAttributes.addFlashAttribute("warning", "Flight ID: " + id + " is deleted successfully.");
			} else if (deletedFlight.getOrder() != null) {
				redirectAttributes.addFlashAttribute("warning", "Flight ID: " + id + " is in the ORDER state.");
			}
			return "redirect:/products/flights/" + product.getId();
		} catch (CategoryNotFoundException e) {
			redirectAttributes.addFlashAttribute("warning", e.getMessage());
			return "redirect:/products";
		}
	}
}
