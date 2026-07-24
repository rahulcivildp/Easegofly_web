package com.easygofly.product;
 
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.product.ProductRepository;
import com.easygofly.admin.user.UserRepository;
import com.easygofly.entity.Brand;
import com.easygofly.entity.Category;
import com.easygofly.entity.City;
import com.easygofly.entity.Product;
import com.easygofly.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class ProductRepositoryTest {

	@Autowired private ProductRepository repo;
	@Autowired private UserRepository userRepo;
	@Autowired private TestEntityManager entityManager;
	
	@Test
	public void testCreateNewProduct() {
		Brand brand = entityManager.find(Brand.class, 1);
		Category category = entityManager.find(Category.class, 1);
		City cityOne = entityManager.find(City.class, 1); 
		City cityTwo = entityManager.find(City.class, 2); 
		
		Product product = new Product();
		product.setName("CCU to BLR GOAIR G8");
		product.setAlias("CCU_to_BLR_GOAIR_G8");
		product.setAirlineCode("G8");
		product.setDuration(180);
		product.setOriginTerminal("T1");
		product.setDestinationTerminal("T2");
		product.setCraft(1);
		product.setJourneyClass("Economy");
		product.setPrice(3000);
		product.setCost(4000);
		product.setBaggage(15);
		product.setCabinBaggage(7);
		product.setFundState(true);
		
		product.setCityOne(cityOne.getCode());
		product.setCityTwo(cityTwo.getCode());
		product.setBrands(brand);
		product.setCategories(category);
		product.setEnabled(true);
		product.setInStock(true);
		product.setCreatedTime(new Date());
		product.setUpdatedTime(new Date());
		
		
		Product savedProduct = repo.save(product);
		
		assertThat(savedProduct).isNotNull();
		assertThat(savedProduct.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllProducts() {
		Iterable<Product> listProducts = repo.findAll();
		
		listProducts.forEach(System.out::println);
	}
	
	@Test
	public void testGetProducts() {
		Integer id = 3;
		Product product = repo.findById(id).get();
		System.out.println(product);
		assertThat(product).isNotNull();
	}
	
	@Test
	public void testUpdateProduct() {
		Integer id = 3;
		Product product = repo.findById(id).get();
		product.setPrice(77000);
		
		repo.save(product);
		
		Product updatedProduct = entityManager.find(Product.class, id);
		
		assertThat(updatedProduct.getPrice()).isNotNull();
	}
	
	@Test
	public void testDeleteProduct() {
		Integer id = 3;
		repo.deleteById(id);
		
		Optional<Product> result = repo.findById(id);
		
		assertThat(!result.isPresent());
	}
	
	@Test
	public void testSaveProductWithDetails() {
		User user = entityManager.find(User.class, 8);
		//System.out.println(repo.findAllByUser(user));
	}
	
	@Test
	public void testProductUpdate() {
		
		
	}
}
