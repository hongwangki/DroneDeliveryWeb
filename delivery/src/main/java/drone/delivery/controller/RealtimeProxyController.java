package drone.delivery.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/realtime")
public class RealtimeProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DRONE_BASE = "http://localhost:8000/api/v_a0_0_1";

    // ------------------------------------------------------
    // ① 주문 생성: 스프링 → 드론 서버 POST 프록시
    // ------------------------------------------------------
    /*@PostMapping("/orders/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            String url = DRONE_BASE + "/orders/create";
            log.info("📦 [CREATE ORDER] Forwarding to drone server: {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, orderData, Map.class);
            log.info("✅ [CREATE ORDER] Response from drone server: {}", response.getStatusCode());

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("❌ [CREATE ORDER] Error while creating order", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Failed to create order on drone server"));
        }
    }*/

    // ------------------------------------------------------
    // ② 상태 데이터 프록시: 프론트 → 스프링 → 드론 서버 GET
    // ------------------------------------------------------
    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<?> getDroneStatus(@PathVariable Long orderId) {
        try {
            String url = DRONE_BASE + "/orders/get_drone_data_by_order/" + orderId;
            log.info("📡 [STATUS PROXY] Requesting: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("✅ [STATUS PROXY] Success {}", response.getStatusCode());
                return ResponseEntity.ok(response.getBody());
            }

            log.warn("⚠️ [STATUS PROXY] Non-OK response: {}", response.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Drone status fetch failed"));

        } catch (Exception e) {
            log.error("❌ [STATUS PROXY] Error fetching drone status", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------------------------------------------
    // ③ 이미지 프록시: 프론트 → 스프링 → 드론 서버 GET (PNG)
    // ------------------------------------------------------
    @GetMapping("/orders/{orderId}/image.png")
    public ResponseEntity<?> getDroneImage(@PathVariable Long orderId) {
        try {
            String url = DRONE_BASE + "/orders/get_drone_data_by_order/" + orderId;
            log.info("🖼 [IMAGE PROXY] Requesting: {}", url);

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ByteArrayResource resource = new ByteArrayResource(response.getBody());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setCacheControl(CacheControl.noStore());
                headers.setPragma("no-cache");

                log.info("✅ [IMAGE PROXY] Success {}", response.getStatusCode());
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            }

            log.warn("⚠️ [IMAGE PROXY] Non-OK response: {}", response.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Drone image fetch failed"));

        } catch (Exception e) {
            log.error("❌ [IMAGE PROXY] Error fetching drone image", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
