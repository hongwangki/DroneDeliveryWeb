package drone.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Data
public class OptionItemForm {
    private String name;
    private Integer priceDelta;   // ← Integer 로 변경 (nullable 허용)
    private Integer stock;
    private Integer displayOrder; // 새 필드 사용
    private Integer sortOrder;    // 레거시 호환 (있으면 같이 둬도 됨)
}