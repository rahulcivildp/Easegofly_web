package com.easygofly.user.category;
 

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
//import java.util.List;
import java.util.Set;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.category.CategoryRepository;
import com.easygofly.entity.Category;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class CategoryRepositoryTest {

	@Autowired
	private CategoryRepository repo;
	
	@Test
	public void testCreateRootCategory() {
		Category category = new Category("Electronics");
		Category savedCategory = repo.save(category);
		
		assertThat(savedCategory.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateSubCategory() {
		Category parent = new Category(7);
		Category categoriesLaptop1 = new Category("iPhone", parent);
		Category categoriesLaptop2 = new Category("Samsung", parent);
		Category categoriesLaptop3 = new Category("OnePlus", parent);
		Category categoriesLaptop4 = new Category("Xaomi", parent);
		Category categoriesLaptop5 = new Category("Asus", parent);
		Category categoriesLaptop6 = new Category("Nokia", parent);
		repo.saveAll(List.of(categoriesLaptop1, categoriesLaptop2, categoriesLaptop3, categoriesLaptop4, categoriesLaptop5, categoriesLaptop6));
		
		//assertThat(subCat.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testGetCategory() {
		Category category = repo.findById(2).get();
		System.out.println(category.getName());
		
		Set<Category> children = category.getChildren();
		for (Category subCategory : children) {
			System.out.println(subCategory.getName());
		}
		
		assertThat(children.size()).isGreaterThan(0);
	}
	
	@Test
	public void testPrintAllCategories() {
		Iterable<Category> categories = repo.findAll();
		for (Category category : categories) {
			if (category.getParent() == null) {
				System.out.println(category.getName());
			
				Set<Category> children = category.getChildren();
				for (Category subCategory : children) {
					System.out.println("--" + subCategory.getName());
					printChildren(subCategory, 1);
				}
			}
		}
	}
	
	
	private void printChildren(Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();
		for (Category subCategory : children) {
			for (int i = 0; i < newSubLevel; i++) {
				System.out.print("--");
			}
			System.out.println(subCategory.getName());
			
			printChildren(subCategory, newSubLevel);
		}
	}
	
	@Test
	public void testListRootCategories() {
		List<Category> rootCategories = repo.findRootCategories();
		rootCategories.forEach(cat -> System.out.println(cat.getName()));
		
		
	}
}
