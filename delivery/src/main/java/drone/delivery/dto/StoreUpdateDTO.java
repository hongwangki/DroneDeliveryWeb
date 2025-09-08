package drone.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreUpdateForm {
    @NotBlank(message = "가게 이름을 입력하세요.")
    private String name;

    private String description;

    @NotBlank(message = "카테고리를 선택하세요.")
    private String category;

    private String imageUrl;

    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private Integer minOrderPrice;
}