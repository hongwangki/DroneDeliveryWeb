package drone.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddToCartRequestDTO {

    @NotNull
    private Long productId;

    @Min(1)
    private Integer quantity = 1;

    /**
     * 선택된 옵션 아이템 id들의 리스트.
     * 템플릿에서 name="options" 로 여러 개 전송됨.
     */
    private List<Long> options = new ArrayList<>();
}
