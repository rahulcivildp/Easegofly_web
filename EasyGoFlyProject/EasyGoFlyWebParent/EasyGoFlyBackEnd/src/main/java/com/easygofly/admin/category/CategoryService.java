package com.easygofly.admin.category;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.easygofly.entity.Category;
import com.easygofly.entity.exception.CategoryNotFoundException;


@Service
@Transactional
public class CategoryService {
	
	public static final int ITEMS_PER_PAGE = 100;
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	public List<Category> listAll() {
		return (List<Category>) categoryRepo.findAll(Sort.by("name").ascending());
	}
	
	//listing of hierarchical categories in category page.
	public List<Category> listAllHierarchicalCategories() {
		List<Category> rootCategories = categoryRepo.findRootCategories();
		return hierarchicalCategories(rootCategories);
	}
	
	public List<Category> hierarchicalCategories(List<Category> rootCategories) {
		List<Category> listhierarchy = new ArrayList<>();
		
		for (Category rootCategory : listhierarchy) {
			listhierarchy.add(Category.copyFull(rootCategory));
			
			Set<Category> children = rootCategory.getChildren();
			for (Category subCategory : children) {
				String name = "--" + subCategory.getName();
				listhierarchy.add(Category.copyFull(subCategory, name));
				
				listChildrenCategories(listhierarchy, subCategory, 1);
			}
		}
		
		return listhierarchy;
	}
	
	private void listChildrenCategories(List<Category> listhierarchy, Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();
		for (Category subCategory : children) {
			String name = "";
			for (int i = 0; i < subLevel+1; i++) {
				name += "--";
			}
			name += subCategory.getName();
			
			listhierarchy.add(Category.copyFull(subCategory, name));
			
			listChildrenCategories(listhierarchy, parent, newSubLevel);
		}
	}
	
	//Show list of categories available in edit/add page
	public List<Category> listCategoriesUsedInForm() {
		List<Category> categoryUsedInForm = new ArrayList<>();
		Iterable<Category> categoryInDB = categoryRepo.findAll();
		
		for (Category category : categoryInDB) {
			if (category.getParent() == null) {
				categoryUsedInForm.add(Category.copyIdAndName(category));
			
				Set<Category> children = category.getChildren();
				for (Category subCategory : children) {
					String name = "--" + subCategory.getName();
					categoryUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
					
					listChildren(categoryUsedInForm, subCategory, 1);
				}
			}
		}
		return categoryUsedInForm;
	}
	
	private void listChildren(List<Category> categoryUsedInForm, Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();
		for (Category subCategory : children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			
			categoryUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			
			listChildren(categoryUsedInForm, subCategory, newSubLevel);
		}
	}
	
	public Page<Category> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sort);
		
		if(keyword != null) {
			return categoryRepo.findCategory(keyword, pageable);
		}
		return categoryRepo.findAll(pageable);
	}
	
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		categoryRepo.updateEnableStatus(id, enabled);
	}

	public Category saveCategory(Category category) {
		Category categoryInDB = categoryRepo.findById(category.getId()).get();
		Category parent = category.getParent();
		
		if (parent != null) {
			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIds += String.valueOf(parent.getId()) + "-";
			category.setAllParentIDs(allParentIds);
		}
		
		if (category.getImage() != null) {
			categoryInDB.setImage(category.getImage());
		}
		
		return categoryRepo.save(category);
	}

	public Category updateCategory(Integer id) throws CategoryNotFoundException {
		try {
			return categoryRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new CategoryNotFoundException("Could not find any user with ID: " + id);
		}
	}

	public void deleteCategory(Integer id) throws CategoryNotFoundException {
		Long count = categoryRepo.countById(id);
		if(count == null || count == 0) {
			throw new CategoryNotFoundException("Could not find any user with ID: " + id);
		}
		
		categoryRepo.deleteById(id);
	}

	public boolean isNameUnique(Integer id, String name) {
		Category userByName = categoryRepo.getUserByName(name);
		
		if (userByName == null) return true;
		
		boolean isCreatingNew = (id == null);
		
		if (isCreatingNew) {
			if (userByName != null) return false;
		} else {
			if (userByName.getId() != id) {
				return false;
			}
		}
		
		return true;
	}
	
	
}
