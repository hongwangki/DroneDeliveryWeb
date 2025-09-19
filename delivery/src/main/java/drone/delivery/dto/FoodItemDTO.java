package drone.delivery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FoodItemDTO {
    private String foodName;  // 음식 이름
    private int quantity;     // 개수
}
