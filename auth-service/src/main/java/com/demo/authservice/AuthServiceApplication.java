package com.demo.authservice;

import com.demo.authservice.entity.Role;
import com.demo.authservice.repository.IRoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

	@Autowired
	private IRoleRepository roleRepository;
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@PostConstruct
	public void initRoles() {
		if (roleRepository.findAll().isEmpty()) {
			roleRepository.save(new Role(null, "ROLE_USER"));
			roleRepository.save(new Role(null, "ROLE_ADMIN"));
		}
	}

}
