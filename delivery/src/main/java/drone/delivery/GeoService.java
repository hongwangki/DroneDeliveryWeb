package drone.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class GeoService {

    private static final Logger logger = LoggerFactory.getLogger(GeoService.class);

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public void updateMemberCoordinates(Member member) {
        if (member == null || member.getAddress() == null) {
            logger.warn("❗ member 또는 address가 null입니다.");
            return;
        }

        String rawQuery = buildStreetOnlyQuery(member.getAddress());
        logger.warn("📍 rawQuery: '{}'", rawQuery);

        if (rawQuery.isBlank()) {
            logger.warn("❗ rawQuery가 비어 있습니다.");
            return;
        }

        try {
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + rawQuery;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey.trim());
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) {
                    return false;
                }
            });

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            logger.info("✅ 응답 상태코드: {}", response.getStatusCode());

            JsonNode documents = response.getBody().get("documents");
            logger.warn("📦 documents: {}", documents);

            if (documents == null || !documents.isArray() || documents.size() == 0) {
                logger.warn("❌ documents 비어 있음. 주소: {}", rawQuery);
                return;
            }

            String target = rawQuery.replaceAll("\\s+", "").toLowerCase(); // 비교 대상
            JsonNode selectedDoc = null;

            for (JsonNode doc : documents) {
                String roadName = doc.path("road_address").path("address_name").asText(null);
                String jibunName = doc.path("address").path("address_name").asText(null);

                String roadClean = roadName != null ? roadName.replaceAll("\\s+", "").toLowerCase() : "";
                String jibunClean = jibunName != null ? jibunName.replaceAll("\\s+", "").toLowerCase() : "";

                logger.info("➡️ 비교: '{}' in '{}' (도로명), '{}' (지번)", target, roadClean, jibunClean);

                if (roadClean.contains(target) || jibunClean.contains(target)) {
                    selectedDoc = doc;
                    break;
                }
            }

            if (selectedDoc == null) {
                logger.warn("❌ '{}' 을 포함하는 어떤 address_name 도 찾지 못함", rawQuery);
                return;
            }

            JsonNode coordNode = selectedDoc.path("road_address").isMissingNode()
                    ? selectedDoc.path("address")
                    : selectedDoc.path("road_address");

            double lng = coordNode.path("x").asDouble();
            double lat = coordNode.path("y").asDouble();

            member.setLongitude(lng);
            member.setLatitude(lat);

            logger.info("🎯 좌표 변환 성공: 위도 {}, 경도 {}", lat, lng);

        } catch (Exception e) {
            logger.error("💥 주소 좌표 변환 중 오류 발생", e);
        }
    }

    private String buildStreetOnlyQuery(Address address) {
        String street = address.getStreet();
        logger.warn("📍 Address.street: '{}'", street);
        return (street != null) ? street.trim().replaceAll("\\s+", " ") : "";
    }
}
