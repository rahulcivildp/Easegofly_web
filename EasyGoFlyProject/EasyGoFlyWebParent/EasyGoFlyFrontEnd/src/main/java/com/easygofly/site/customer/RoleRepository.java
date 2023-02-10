package com.easygofly.site.customer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.easygofly.entity.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {

}
