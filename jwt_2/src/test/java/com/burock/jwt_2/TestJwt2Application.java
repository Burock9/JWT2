package com.burock.jwt_2;

import org.springframework.boot.SpringApplication;

public class TestJwt2Application {

	public static void main(String[] args) {
		SpringApplication.from(Jwt2Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
