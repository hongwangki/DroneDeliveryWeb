package drone.delivery.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drone") // 드론이 호출하는 prefix
public class DroneWebHookController {

    private final DroneEventHandler handler;

    // 드론이 완료를 통지: POST /drone/orders/{orderId}/delivered
    @PostMapping("/orders/{orderId}/delivered")
    public ResponseEntity<?> delivered(@PathVariable Long orderId, @RequestBody(required=false) Map<String,Object> payload) {
        handler.handleDelivered(orderId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // (옵션) 상태 웹훅이 따로 있고 phase=DELIVERED일 때 완료 처리
    @PostMapping("/orders/{orderId}/status")
    public ResponseEntity<?> status(@PathVariable Long orderId, @RequestBody Map<String,Object> body) {
        String phase = String.valueOf(body.getOrDefault("phase",""));
        if ("DELIVERED".equalsIgnoreCase(phase)) {
            handler.handleDelivered(orderId);
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }
}