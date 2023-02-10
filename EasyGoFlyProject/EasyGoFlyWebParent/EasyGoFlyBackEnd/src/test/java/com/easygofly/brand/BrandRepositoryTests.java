package com.easygofly.brand;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.brand.BrandRepositoy;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class BrandRepositoryTests {
	
	@Autowired
	private BrandRepositoy repo;
	
	@Autowired
	private TestEntityManager enityManager;
	
	@Test
	public void testCreateBrands() {
		Category categoryAdmin1 = enityManager.find(Category.class, 1);
		Category categoryAdmin2 = enityManager.find(Category.class, 2);
		Category categoryAdmin3 = enityManager.find(Category.class, 3);
		Brand acer = new Brand(1, "Acer", "brand-logo.png");
		Brand apple = new Brand(2, "Apple", "brand-logo.png");
		Brand samsung = new Brand(3, "Samsung", "brand-logo.png");
		acer.addCategory(categoryAdmin1);
		apple.addCategory(categoryAdmin2);
		samsung.addCategory(categoryAdmin3);
		
		repo.saveAll(List.of(acer, apple, samsung));
		
		Iterable<Brand> findAll = repo.findAll();
		assertThat(findAll).size().isGreaterThan(0);
	}

	@Test
	public void testCreateUserWithTwoRoles1() {
		Brand acer = new Brand(1, "Acer", "brand-logo.png");
		Brand apple = new Brand(2, "Apple", "brand-logo.png");
		Brand samsung = new Brand(3, "Samsung", "brand-logo.png");
		Category categoryLaptop = new Category(6);
		Category categoryCellphone = new Category(4);
		Category categoryTablets = new Category(7);
		Category categoryMemory = new Category(29);
		Category categoryHDD = new Category(24);
		acer.addCategory(categoryLaptop);
		apple.addCategory(categoryCellphone);
		apple.addCategory(categoryTablets);
		samsung.addCategory(categoryMemory);
		samsung.addCategory(categoryHDD);
		
		repo.saveAll(List.of(acer, apple, samsung));
		
		Iterable<Brand> findAll = repo.findAll();
		assertThat(findAll).size().isGreaterThan(0);
	}
	
	@Test
	public void testListAllBrands() {
		Iterable<Brand> listCategories = repo.findAll();
		listCategories.forEach(brand -> System.out.println(brand));
	}
	
	@Test
	public void testBrandById() {
		Brand userName = repo.findById(9).get();
		System.out.println(userName	);
		assertThat(userName).isNotNull();
	}
	
	@Test
	public void testUpdateBrandCategory() {
		Brand currentBrand = repo.findById(11).get();
		currentBrand.setName("Samsung Electronics");
		
		repo.save(currentBrand);
	}
	
	@Test
	public void testDeleteBrand() {
		Integer brnadID = 10;
		repo.deleteById(brnadID);
	}
}
