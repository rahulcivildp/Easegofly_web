package com.easygofly.admin.brand;

import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.admin.category.CategoryRepository;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;
import com.easygofly.entity.exception.BrandNotFoundException;


@Service
@Transactional
public class BrandService {
	
	public static final int BRAND_PER_PAGE = 10;
	
	@Autowired
	private BrandRepositoy brandRepo;
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	public List<Brand> listAll() {
		return (List<Brand>) brandRepo.findAll(Sort.by("firstName").ascending());
	}
	
	public Page<Brand> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, BRAND_PER_PAGE, sort);
		
		if(keyword != null) {
			return brandRepo.findBrand(keyword, pageable);
		}
		return brandRepo.findAll(pageable);
	}
	
	public void updateUserEnabledStatus(Integer id, boolean enabled) {
		brandRepo.updateEnableStatus(id, enabled);
	}
	
	public Brand updateBrand(Integer id) throws BrandNotFoundException {
		try {
			return brandRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new BrandNotFoundException("Could not find any brand with ID: " + id);
		}
	}
	
	public List<Category> listCategories() {
		return (List<Category>) categoryRepo.findAll();
	}
	
	public Brand saveBrand(Brand brand) {
		return brandRepo.save(brand);
	}
	
	public void deleteUser(Integer id) throws BrandNotFoundException {
		Long count = brandRepo.countById(id);
		if(count == null || count == 0) {
			throw new BrandNotFoundException("Could not find any brand with ID: " + id);
		}
		
		brandRepo.deleteById(id);
	}
	
	public boolean isNameUnique(Integer id, String name) {
		Brand brandByName = brandRepo.getBrandByName(name);
		
		if (brandByName == null) return true;
		
		boolean isCreatingNew = (id == null);
		
		if (isCreatingNew) {
			if (brandByName != null) return false;
		} else {
			if (brandByName.getId() != id) {
				return false;
			}
		}
		
		return true;
	}
	
}
