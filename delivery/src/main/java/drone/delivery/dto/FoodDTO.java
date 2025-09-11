package drone.delivery.dto;

import drone.delivery.domain.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodDTO {

    @NotBlank(message = "메뉴명을 입력하세요.")
    private String foodName;

    @NotNull(message = "가격을 입력하세요.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer foodPrice;

    @NotNull(message = "수량을 입력하세요.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;

    private String productImageUrl;

    /** DTO -> 엔티티 변환 헬퍼 */
    public Product toEntity() {
        Product p = new Product();
        p.setFoodName(foodName);
        p.setFoodPrice(foodPrice);
        p.setQuantity(quantity);
        p.setProductImageUrl(productImageUrl);
        return p;
    }
}