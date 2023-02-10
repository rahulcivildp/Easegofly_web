package com.easygofly.admin.product;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.admin.brand.BrandRepositoy;
import com.easygofly.admin.category.CategoryRepository;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;
import com.easygofly.entity.Product;
import com.easygofly.entity.User;
import com.easygofly.entity.exception.ProductNotFoundException;
import com.easygofly.entity.exception.UserNotFoundException;


@Service
@Transactional
public class ProductService {
	
	public static final int PRODUCTS_PER_PAGE = 8;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	@Autowired
	private BrandRepositoy brandRepo;
	
	public List<Product> listAll() {
		return (List<Product>) productRepo.findAll(Sort.by("name").ascending());
	}
	
	public Page<Product> listByPage(int pageNum, String sortField, String sortDir, String keyword, Integer categoryId) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sort);
		
		if(keyword != null && !keyword.isEmpty()) {
			if(categoryId != null && categoryId > 0) {
				String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
				return productRepo.searchInCategory(categoryId, categoryIdMatch, keyword, pageable);
			}
			return productRepo.findProduct(keyword, pageable);
		}
		
		if(categoryId != null && categoryId > 0) {
			String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
			return productRepo.findAllInCategory(categoryId, categoryIdMatch, pageable);
		}
		
		return productRepo.findAll(pageable);
	}
	
	public Page<Product> listByPageUser(int pageNum, String sortField, String sortDir, String keyword, Integer categoryId, User user) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sort);
		
		if(keyword != null && !keyword.isEmpty()) {
			return productRepo.findAllByUser(user, pageable);
		}
		
		return productRepo.findAllByUser(user, pageable);
	}
	
	public void saveProductPrice(Product product) {
		Product productInDB = productRepo.findById(product.getId()).get();
		productInDB.setCost(product.getCost());
		productInDB.setPrice(product.getPrice());
		productInDB.setDiscountPercentage(product.getDiscountPercentage());
		
		productRepo.save(productInDB);
	}
	
	public void updateUserEnabledStatus(Integer id, boolean enabled) {
		productRepo.updateEnableStatus(id, enabled);
	}
	
	public List<Category> listCategory() {
		return (List<Category>) categoryRepo.findAll();
	}
	
	public List<Brand> listBrand() {
		return (List<Brand>) brandRepo.findAll();
	}

	public Product saveProduct(Product product, User user) {
		
		boolean isUpdatingUser = (product.getId() != 0);
		
		if(isUpdatingUser) {
			product.setCreatedTime(new Date());
		}
	
		if(product.getAlias() == null || product.getAlias().isEmpty()) {
			String setDefault = product.getName().replace(" ", "-");
			product.setAlias(setDefault);
		} else {
			product.setAlias(product.getAlias().replace(" ", "-"));
		}
		
		product.setUpdatedTime(new Date());
		product.setUser(user);
		
		return productRepo.save(product);
	}
	
	public Product saveProductDetails(Product product) {
		Product productInDB = productRepo.findById(product.getId()).get();
		
		productInDB.setDetails(product.getDetails());
		productInDB.setUpdatedTime(new Date());
		
		return productRepo.save(productInDB);
	}
	
	public Product updateProduct(Integer id) throws UserNotFoundException {
		try {
			return productRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new UserNotFoundException("Could not find any user with ID: " + id);
		}
	}

	public void deleteProduct(Integer id) throws UserNotFoundException {
		Long count = productRepo.countById(id);
		if(count == null || count == 0) {
			throw new UserNotFoundException("Could not find any user with ID: " + id);
		}
		productRepo.deleteById(id);
	}

	public String isNameUnique(Integer id, String name) {
		Product productByName = productRepo.getProductByName(name);
		
		if(productByName == null) return "OK";
		
		boolean isCreatingNew = (id == null || id == 0);
		
		if (isCreatingNew) {
			if (productByName != null) {
				return "Duplicate";
			}
		} else {
			if (productByName.getId() != id) {
				return "Duplicate";
			}
		}
		
		return "OK";
	}
	
	public Product getProductDetails(Integer id) throws ProductNotFoundException {
		try {
			return productRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new ProductNotFoundException("Could not find any product with ID: " + id);
		}
	}
	
}
