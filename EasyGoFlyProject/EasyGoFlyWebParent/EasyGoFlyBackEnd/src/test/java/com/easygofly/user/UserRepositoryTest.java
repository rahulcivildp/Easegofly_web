package com.easygofly.user;
 
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import com.easygofly.admin.EasyGoFlyBackEndApplication;
import com.easygofly.admin.user.UserRepository;
import com.easygofly.entity.Role;
import com.easygofly.entity.User;


@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
@ContextConfiguration(classes = EasyGoFlyBackEndApplication.class)
public class UserRepositoryTest {
	
	@Autowired
	private UserRepository repo;
	
	@Autowired
	private TestEntityManager enityManager;
	
	@Test
	public void testCreateUserWithOneRole() {
		Role roleAdmin = enityManager.find(Role.class, 1);
		User userNameHM = new User("rahulcivildp@gmail.com", "tanmay@111", "Tanmay", "Sarkar");
		userNameHM.addRole(roleAdmin);
		
		User savedUser = repo.save(userNameHM);
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateUserWithTwoRoles1() {
		User userAbu = new User("nam@codejava.net", "nam2020", "Nam", "Ha Minh");
		Role roleEditor = new Role(1);
		Role roleAssistance = new Role(2);
		userAbu.addRole(roleEditor);
		userAbu.addRole(roleAssistance);
		
		User savedUser = repo.save(userAbu);
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateUserWithTwoRoles2() {
		User userAbu = new User("soumitra1992@gmail.com", "polu2020", "Soumitra", "Ghosh");
		Role roleEditor = new Role(2);
		Role roleAssistance = new Role(4);
		userAbu.addRole(roleEditor);
		userAbu.addRole(roleAssistance);
		
		User savedUser = repo.save(userAbu);
		assertThat(savedUser.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllUsers() {
		Iterable<User> listUsers = repo.findAll();
		listUsers.forEach(user -> System.out.println(user));
	}
	
	@Test
	public void testUserById() {
		User userName = repo.findById(2).get();
		System.out.println(userName	);
		assertThat(userName).isNotNull();
	}
	
	@Test
	public void testUpdateUserDetails() {
		User userName = repo.findById(2).get();
		userName.setEnabled(true);
		userName.setEmail("tanmay.sarkar2@dxc.com");
		
		repo.save(userName);
	}
	
	@Test
	public void testUpdateUserRole() {
		User currentUser = repo.findById(2).get();
		Role roleEditor = new Role(3);
		Role roleSalesPerson = new Role(4);
		currentUser.getRoles().remove(roleEditor);
		currentUser.addRole(roleSalesPerson);
		
		repo.save(currentUser);
	}
	
	@Test
	public void testDeleteUser() {
		Integer userID = 5;
		repo.deleteById(userID);
	}
	
	@Test
	public void testGetUserByEmail() {
		String email = "tanmay.sarkar2@dxc.com";
		User userByEmail = repo.getUserByEmail(email);
		
		assertThat(userByEmail).isNotNull();
	}
	
	@Test
	public void testCountById() {
		Integer id = 1;
		Long count = repo.countById(id);
		
		assertThat(count).isNotNull().isGreaterThan(0);
	}
	
	@Test
	public void testDisableUser() {
		Integer id = 12;
		repo.updateEnableStatus(id, true);
	}
	
	@Test
	public void testListFirstPage() {
		int pageNumber = 1;
		int pageSize = 4;
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = repo.findAll(pageable);
		
		List<User> listUsers = page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		
		assertThat(listUsers.size()).isEqualTo(pageSize);
	}
	
	@Test
	public void testSearchUser() {
		String keyword = "bruce";
		int pageNumber = 0;
		int pageSize = 4;
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<User> page = repo.findUser(keyword, pageable);
		
		List<User> listUsers = page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		
		assertThat(listUsers.size()).isGreaterThan(0);
	}
	
	@Test
	public void testLog() {
		List<String> logs = new ArrayList<>();
		logs.add("a b");
		logs.add("a c");
		logs.add("b c");
		logs.add("c d");
		
		Integer threshold = 2;
		
		List<String> strThresold = new ArrayList<String>();
	    Map<String, Integer> mapStr = new TreeMap<>();
	    for (String log: logs) {
	        String[] logStr = log.split(" ");
	        for(int i = 0; i < logs.size(); i++) {
	            for (Map.Entry<String, Integer> entry : mapStr.entrySet()) {
	                if(!entry.getKey().equals(logStr[0])) {
	                    mapStr.put(logStr[0], 1);
	                } else {
	                    mapStr.put(logStr[0], i);
	                }
	                
	                if(!entry.getKey().equals(logStr[1])) {
	                    mapStr.put(logStr[0], 1);
	                } else {
	                    mapStr.put(logStr[0], i);
	                }
	            }
	        }
	    }
	    
	    
	    for (Map.Entry<String, Integer> entry : mapStr.entrySet()) {
	        if(entry.getValue() >= threshold) {
	            strThresold.add(entry.getKey());
	        } 
	    }
		
	}
}
