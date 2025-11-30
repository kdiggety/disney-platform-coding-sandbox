package interview.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PropertySourceDebugTest {

    @Autowired ConfigurableEnvironment env;

    @Test
    void showPropertySourceWinners() {
        String[] keys = {
                "app.jwt.trusted-issuers[0]",
                "app.jwt.hmac-secrets[https://issuer-a.example.com]"
        };

        for (String key : keys) {
            for (PropertySource<?> ps : env.getPropertySources()) {
                Object v = ps.getProperty(key);
                if (v != null) System.out.println(key + " :: " + ps.getName() + " -> " + v);
            }
            System.out.println(key + " :: Effective -> " + env.getProperty(key));
        }
    }
}