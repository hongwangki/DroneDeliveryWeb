package drone.delivery;

import drone.delivery.domain.Member;
import lombok.Value;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoService {

//    @Value("${google.api.key}") //실제 api키로 수정
    private String apiKey;

    private static final String GOOGLE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";

    public void updateMemberCoordinates(Member member) throws JSONException {
        if (member == null || member.getAddress() == null) {
            return; // 주소가 없는 경우 처리하지 않음
        }

        String fullAddress = String.format("%s, %s, %s",
                member.getAddress().getStreet(),
                member.getAddress().getCity(),
                member.getAddress().getZipcode());

        String url = String.format(GOOGLE_GEOCODE_URL, fullAddress.replace(" ", "+"), apiKey);
        RestTemplate restTemplate = new RestTemplate();
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
    }
}