package com.kimhong.job_portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", // Uses an in-memory test database
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.mail.host=localhost",
		"spring.mail.username=mocked-email@test.com",
		"spring.mail.password=mockedpassword",
		"jwt.secret=9a4f434520bed2b0d7b224164b1d56cc3f69ed6260a1e0b3ef1d48c823528b7e" // Dummy 256-bit key
})
class JobPortalApplicationTests {

	@Test
	void contextLoads() {
	}

}
