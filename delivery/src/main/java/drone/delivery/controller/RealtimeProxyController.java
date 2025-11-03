package drone.delivery.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeProxyController {

    private final WebClient droneWebClient;
    private final int timeoutSec;
    private final String authToken; // nullable

    public RealtimeProxyController(
            WebClient droneWebClient,
            @Value("${app.drone.timeout-seconds:3}") int timeoutSec,
            @Value("${app.drone.auth-token:}") String authToken
    ) {
        this.droneWebClient = droneWebClient;
        this.timeoutSec = timeoutSec;
        this.authToken = (authToken != null && !authToken.isBlank()) ? authToken : null;
    }

    @GetMapping(value = "/{orderId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStatus(@PathVariable Long orderId) {
        WebClient.RequestHeadersSpec<?> req = droneWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{orderId}/status").build(orderId));
        if (authToken != null) req = req.header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

        String body = req.retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(timeoutSec)); // 간단화를 위해 block

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    @GetMapping(value = "/{orderId}/image.png")
    public ResponseEntity<byte[]> getImage(@PathVariable Long orderId) {
        WebClient.RequestHeadersSpec<?> req = droneWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{orderId}/image").build(orderId))
                .accept(MediaType.IMAGE_PNG);
        if (authToken != null) req = req.header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);

        byte[] bytes = req.retrieve()
                .bodyToMono(byte[].class)
                .block(Duration.ofSeconds(timeoutSec));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS).noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(bytes);
    }
}
