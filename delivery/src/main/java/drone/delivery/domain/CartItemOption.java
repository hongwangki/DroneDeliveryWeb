package drone.delivery.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartItemOption {
    private Long optionItemId;  // 원본 추적용
    private String name;        // 스냅샷
    private int priceDelta;     // 스냅샷
    private Integer quantity;   // 수량형 옵션이면 사용(기본 1)
}