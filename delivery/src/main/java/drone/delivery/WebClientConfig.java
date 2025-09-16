package drone.delivery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * 파이썬 서버로 쏴주는 경로
 */
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient pythonClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }
}
