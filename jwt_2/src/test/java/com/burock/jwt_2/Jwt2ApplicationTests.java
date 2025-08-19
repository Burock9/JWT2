package com.burock.jwt_2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class Jwt2ApplicationTests {

	@Test
	void contextLoads() {
	}

}
