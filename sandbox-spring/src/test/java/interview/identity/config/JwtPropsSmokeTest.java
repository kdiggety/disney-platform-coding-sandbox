package interview.identity.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtPropsSmokeTest {
    @Autowired
    JwtProperties props;

    @Test
    void loads() {
        System.out.println(props.getIssuers());
    }
}
