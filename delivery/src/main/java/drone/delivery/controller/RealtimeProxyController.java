package drone.delivery.controller;


import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/realtime")
public class RealtimeProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    // ------------------------------------------------------
    // ✅ 기존 ①: 상태 프록시
    // ------------------------------------------------------
    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<?> getStatus(@PathVariable Long orderId) {
        try {
            String droneServer = "http://localhost:8000"; // FastAPI 서버 주소
            String url = droneServer + "/orders/" + orderId + "/status";

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            log.info("STATUS GET : {}", response );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching drone status", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Failed to fetch drone status"));
        }
    }

    // ------------------------------------------------------
    // ✅ 기존 ②: 이미지 프록시
    // ------------------------------------------------------
    @GetMapping("/orders/{orderId}/image")
    public ResponseEntity<?> getImage(@PathVariable Long orderId) {
        try {
            String droneServer = "http://localhost:8000";
            String url = droneServer + "/orders/" + orderId + "/image";

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            ByteArrayResource resource = new ByteArrayResource(response.getBody());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            log.info("IMAGE GET : {}", response );

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching drone image", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Failed to fetch drone image"));
        }
    }

    // ------------------------------------------------------
    // 🆕 추가 ③: 드론이 직접 POST하는 수신 엔드포인트
    // ------------------------------------------------------
    @PostMapping(value = "/drone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> receiveDroneData(
            @RequestParam("drone_id") String droneId,
            @RequestParam("vehicle_name") String vehicleName,
            @RequestParam("state") String stateJson,
            @RequestParam(value = "current_order", required = false) String currentOrder,
            @RequestParam(value = "path", required = false) String pathJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            // JSON 파싱
            JSONObject state = new JSONObject(stateJson);
            JSONObject gps = state.getJSONObject("gps_location");
            double lat = gps.optDouble("lat");
            double lon = gps.optDouble("lon");
            double alt = gps.optDouble("alt");

            log.info("📡 [DRONE POST] id={} vehicle={} lat={} lon={} alt={}",
                    droneId, vehicleName, lat, lon, alt);

            // ✅ OS/환경에 관계없이 동작하는 업로드 경로
            Path baseDir = Paths.get(System.getProperty("user.home"), "drone_uploads");
            Files.createDirectories(baseDir); // 없으면 자동 생성

            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = "drone_" + droneId + ".png";
                Path savePath = baseDir.resolve(filename);
                imageFile.transferTo(savePath.toFile());
                log.info("📷 Drone image saved: {}", savePath.toAbsolutePath());
            }

            // 필요 시 내부 캐시에도 저장 가능
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("receiveDroneData error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
