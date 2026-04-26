package OMS_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=my-dummy-jwt-secret-key-for-testing-purposes-only")
class OmsBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
