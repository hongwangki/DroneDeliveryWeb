package drone.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import drone.delivery.domain.Store;
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

            // 🔻 변경: 가장 유사한 한 건만 선택
            JsonNode selectedDoc = pickBestFromDocuments(rawQuery, documents);
            if (selectedDoc == null) {
                logger.warn("❌ '{}' 과 충분히 매칭되는 문서를 찾지 못함", rawQuery);
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

    // 기존 GeoService 안에 아래 메서드만 추가하세요.
    public void updateStoreCoordinates(Store store) {
        if (store == null || store.getAddress() == null) {
            logger.warn("❗ store 또는 address가 null입니다.");
            return;
        }
        try {
            // Member용과 동일한 방식: street 기반 질의
            String rawQuery = buildStreetOnlyQuery(store.getAddress());
            logger.warn("📍 rawQuery(store): '{}'", rawQuery);

            if (rawQuery.isBlank()) {
                logger.warn("❗ rawQuery가 비어 있습니다. (store)");
                return;
            }

            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + rawQuery;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey.trim());
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) { return false; }
            });

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class
            );

            logger.info("✅ (store) 응답 상태코드: {}", response.getStatusCode());

            JsonNode documents = response.getBody().get("documents");
            logger.warn("📦 (store) documents: {}", documents);

            if (documents == null || !documents.isArray() || documents.size() == 0) {
                logger.warn("❌ (store) documents 비어 있음. 주소: {}", rawQuery);
                return;
            }

            // 🔻 변경: 가장 유사한 한 건만 선택
            JsonNode selectedDoc = pickBestFromDocuments(rawQuery, documents);
            if (selectedDoc == null) {
                logger.warn("❌ (store) '{}' 과 충분히 매칭되는 문서를 찾지 못함", rawQuery);
                return;
            }

            JsonNode coordNode = selectedDoc.path("road_address").isMissingNode()
                    ? selectedDoc.path("address")
                    : selectedDoc.path("road_address");

            double lng = coordNode.path("x").asDouble();
            double lat = coordNode.path("y").asDouble();

            store.setLongitude(lng);
            store.setLatitude(lat);

            logger.info("🎯 (store) 좌표 변환 성공: 위도 {}, 경도 {}", lat, lng);

        } catch (Exception e) {
            logger.error("💥 (store) 주소 좌표 변환 중 오류 발생", e);
        }
    }

    /* =========================
       유사도 기반 선택 유틸
       ========================= */

    // 공백/기호 제거 + 소문자
    private String norm(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s,\\-]", "").toLowerCase();
    }

    // 간단 유사도 점수
    // 정확=100, prefix=60, contains=40, 공통 접두 길이(최대 20)
    private int similarity(String target, String cand) {
        if (cand == null || cand.isEmpty()) return 0;
        if (target.equals(cand)) return 100;
        if (cand.startsWith(target) || target.startsWith(cand)) return 60;
        if (cand.contains(target) || target.contains(cand)) return 40;
        int n = Math.min(target.length(), cand.length());
        int i = 0; while (i < n && target.charAt(i) == cand.charAt(i)) i++;
        return Math.min(i, 20);
    }

    // documents에서 베스트 한 건 고르기
    private JsonNode pickBestFromDocuments(String rawQuery, JsonNode documents) {
        String target = norm(rawQuery);

        int bestScore = Integer.MIN_VALUE;
        JsonNode best = null;

        for (JsonNode doc : documents) {
            String roadAddr = doc.path("road_address").path("address_name").asText(null);
            String jibunAddr = doc.path("address").path("address_name").asText(null);

            String roadName = doc.path("road_address").path("road_name").asText("");
            String mainNo   = doc.path("road_address").path("main_building_no").asText("");
            String zoneNo   = doc.path("road_address").path("zone_no").asText("");
            String building = doc.path("road_address").path("building_name").asText("");

            String composed  = (roadName + mainNo).trim();        // 예: 세종대로110
            String composed2 = (roadName + " " + mainNo).trim();   // 예: 세종대로 110

            String nRoad   = norm(roadAddr);
            String nJibun  = norm(jibunAddr);
            String nComp   = norm(composed);
            String nComp2  = norm(composed2);
            String nBuild  = norm(building);

            int score = 0;
            score = Math.max(score, similarity(target, nRoad));
            score = Math.max(score, similarity(target, nJibun));
            score = Math.max(score, similarity(target, nComp));
            score = Math.max(score, similarity(target, nComp2));
            score = Math.max(score, similarity(target, nBuild));

            if (!zoneNo.isBlank() && target.contains(zoneNo)) score += 10; // 우편번호 보너스
            if (!doc.path("road_address").isMissingNode()) score += 5;     // 도로명 주소 가산

            logger.info("🧮 score={} (road='{}', jibun='{}')", score, roadAddr, jibunAddr);

            if (score > bestScore) {
                bestScore = score;
                best = doc;
            }
        }

        // 너무 낮으면 road_address 가진 첫 문서로 폴백
        if (best == null || bestScore < 20) {
            for (JsonNode doc : documents) {
                if (!doc.path("road_address").isMissingNode()) return doc;
            }
            return documents.get(0);
        }
        return best;
    }
}