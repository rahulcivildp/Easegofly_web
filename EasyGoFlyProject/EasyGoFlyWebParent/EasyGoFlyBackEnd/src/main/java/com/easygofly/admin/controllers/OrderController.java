package com.easygofly.admin.controllers;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.easygofly.admin.brand.BrandRepositoy;
import com.easygofly.admin.cartItem.CartItemService;
import com.easygofly.admin.order.OrderRepository;
import com.easygofly.admin.order.OrderService;
import com.easygofly.admin.order.TravelerRepository;
import com.easygofly.admin.order.export.OrderPDFExporter;
import com.easygofly.admin.setting.GeneralSettingBag;
import com.easygofly.admin.setting.SettingService;
import com.easygofly.admin.setting.city.CityService;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;
import com.easygofly.entity.City;
import com.easygofly.entity.Order;
import com.easygofly.entity.OrderStatus;
import com.easygofly.entity.ProductDetail;
import com.easygofly.entity.TravellerDetail;

@Controller
public class OrderController {

	@Autowired private OrderService orderService;
	@Autowired private OrderRepository orderRepo;
	@Autowired private CityService cityService;
	@Autowired private SettingService settingService;
	@Autowired private TravelerRepository travelerRepo;
	@Autowired private CartItemService cartItemService;
	@Autowired private BrandRepositoy brandRepo;
	@Autowired private EntityManager entityManager;
	
	@GetMapping("/orders")
	public String getAllOrders(Model model, Order order) {
		return listByPage(1, model, "id", "desc", null, order);
	}
	
	@GetMapping("/orders/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model, @Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword, Order order) {
		//System.out.println("Sort Field: " + sortField);
		//System.out.println("Sort Order: " + sortDir);
		Page<Order> pageOrder = orderService.listByPage(pageNum, sortField, sortDir, keyword);
		
		List<Order> listOrders = pageOrder.getContent();
		
		long startCount = (pageNum - 1) * OrderService.ORDER_PER_PAGE + 1;
		long endCount = startCount + OrderService.ORDER_PER_PAGE - 1;
		if (endCount > pageOrder.getTotalElements()) {
			endCount = pageOrder.getTotalElements();
		}
		
		String reverseSort = sortDir.equals("asc") ? "desc" : "asc";
		
		
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageOrder.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageOrder.getTotalElements());
		model.addAttribute("orders", listOrders);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSort", reverseSort);
		model.addAttribute("keyword", keyword);;
		model.addAttribute("successful", OrderStatus.SUCCESSFULL);
		model.addAttribute("pending", OrderStatus.PENDING);
		
		return "orders/orders";
	}
	
	@GetMapping("/order/export_pdf/{id}")
	public void exportToPDF(HttpServletResponse response, @PathVariable("id") Integer id) throws Exception {
		Order order = orderRepo.findById(id).get();
		ProductDetail productDetail = order.getProductDetail();
		OrderPDFExporter exporter = new OrderPDFExporter();
		City city1 = cityService.findCityOneByCode(order);
		City city2 = cityService.findCityTwoByCode(order);
		GeneralSettingBag settingBag = settingService.getGeneralSettingBag();
		String logoLink = settingBag.getSiteLogo();
		String faviconLink = settingBag.getFavicon();
		Brand brand = brandRepo.getBrandByName(productDetail.getBrand());
		Category category = entityManager.find(Category.class, 1);
		
		if (brand == null) {
			brand = new Brand(productDetail.getBrand());
			brand.addCategory(category);
		} 
		
		List<TravellerDetail> travellers = travelerRepo.findTravellerByProductDetailAndOrder(productDetail, order);

		exporter.export(order, response, city1, city2, logoLink, travellers, faviconLink, brand); 

	}
	
	@PostMapping("/orders/successful")
	public String orderSuccessful(@RequestParam(name = "order_id") Integer order_id) {
		orderService.updateStatus(order_id, OrderStatus.SUCCESSFULL);
		
		return "redirect:/orders";
	}
	
	@PostMapping("/orders/pending")
	public String orderPending(@RequestParam(name = "order_id") Integer order_id) {
		orderService.updateStatus(order_id, OrderStatus.PENDING);
		
		return "redirect:/orders";
	}
	
	@GetMapping("/cart-offline")
	public String cartOffline() {
		cartItemService.updateModeOffline();
		return "redirect:/orders";
	}
	
	@GetMapping("/cart-online")
	public String cartOnline() {
		cartItemService.updateModeOnline();
		return "redirect:/orders";
	}
}
