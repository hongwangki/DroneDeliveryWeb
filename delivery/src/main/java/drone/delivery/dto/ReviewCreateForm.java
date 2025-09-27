package drone.delivery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewCreateForm {
    @NotNull
    private Long orderId;
    @NotNull private Long storeId;
    @NotNull(message = "별점을 등록해주세요.") @Min(1) @Max(5) private Integer rating;
    @NotBlank
    @Size(max = 1000) private String content;
}
