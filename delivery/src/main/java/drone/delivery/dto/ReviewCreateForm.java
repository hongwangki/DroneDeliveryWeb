package drone.delivery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewCreateForm {
    @NotNull
    private Long orderId;
    @NotNull private Long storeId;
    @NotNull @Min(1) @Max(5) private Integer rating;
    @NotBlank
    @Size(max = 1000) private String content;
}
