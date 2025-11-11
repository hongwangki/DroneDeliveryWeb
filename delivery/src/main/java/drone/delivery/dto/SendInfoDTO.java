package drone.delivery.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SendInfoDTO {

    //멤버 위도 경도
    private Double UserLatitude;  // 위도
    private Double UserLongitude; // 경도

    //가게 위도 경도
    private Double StoreLatitude;  // 위도
    private Double StoreLongitude; // 경도

    private Long orderId;


    // 음식과 개수 (여러 개)
    private List<FoodItemDTO> items;
}


