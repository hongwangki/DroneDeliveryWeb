package drone.delivery;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
/**
 * 파이썬 서버로 쏴주는 경로
 */
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient droneWebClient(
            @Value("${app.drone.base-url}") String baseUrl,
            @Value("${app.drone.timeout-seconds:3}") int timeoutSec
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(timeoutSec))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSec * 1000)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeoutSec)));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // PNG 대비 버퍼 상향
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {
                    System.out.println("[DroneWebClient] " + request.method() + " " + request.url());
                    return next.exchange(request);
                })
                .exchangeStrategies(strategies)
                .build();
    }


}
