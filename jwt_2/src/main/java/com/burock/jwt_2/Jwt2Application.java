package com.burock.jwt_2;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.burock.jwt_2.model.Role;
import com.burock.jwt_2.model.User;
import com.burock.jwt_2.repository.UserRepository;

@SpringBootApplication
public class Jwt2Application {

	public static void main(String[] args) {
		SpringApplication.run(Jwt2Application.class, args);
	}

	@Bean
	CommandLineRunner initUsers(UserRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			if (userRepo.findByUsername("admin").isEmpty()) {
				userRepo.save(User.builder().username("admin").password(encoder.encode("admin"))
						.roles(Set.<Role>of(Role.ROLE_ADMIN, Role.ROLE_USER)).build());
			}
			if (userRepo.findByUsername("user").isEmpty()) {
				userRepo.save(User.builder().username("user").password(encoder.encode("user"))
						.roles(Set.<Role>of(Role.ROLE_USER)).build());
			}
		};
	}

}
