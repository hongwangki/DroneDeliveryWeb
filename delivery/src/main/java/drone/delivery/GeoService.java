package drone.delivery;

import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import lombok.Value;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeoService {

    private static final Logger logger = LoggerFactory.getLogger(GeoService.class);

//    @Value("${google.api.key}") // application.properties에서 API 키 주입
    private String apiKey;

    private static final String GOOGLE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";

    public void updateMemberCoordinates(Member member) throws JSONException {
        if (member == null || member.getAddress() == null) {
            return; // 주소가 없는 경우 처리하지 않음
        }

        Address address = member.getAddress();
        StringBuilder fullAddress = new StringBuilder();

        // 각 필드가 존재하는 경우에만 추가
        if (address.getStreet() != null && !address.getStreet().isEmpty()) {
            fullAddress.append(address.getStreet()).append(", ");
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            fullAddress.append(address.getCity()).append(", ");
        }
        if (address.getZipcode() != null && !address.getZipcode().isEmpty()) {
            fullAddress.append(address.getZipcode());
        }

        if (fullAddress.length() == 0) {
            return; // 유효한 주소 정보가 없으면 아무것도 하지 않음
        }

        String url = String.format(GOOGLE_GEOCODE_URL, fullAddress.toString().replace(" ", "+"), apiKey);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());

            if ("OK".equals(jsonResponse.getString("status"))) {
                JSONObject location = jsonResponse.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                // Member의 latitude, longitude 업데이트
                member.setLatitude(lat);
                member.setLongitude(lng);
            }
        } catch (Exception e) {
            logger.error("Error while fetching coordinates", e); // 오류 로그 추가
        }
    }
}
