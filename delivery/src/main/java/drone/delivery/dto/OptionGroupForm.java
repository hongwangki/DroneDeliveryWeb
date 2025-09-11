package drone.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OptionGroupForm {
    @NotBlank
    private String name;
    private boolean required;      // 필수 선택 여부
    private boolean multiSelect;   // 다중 선택 가능 여부
    @Min(0) private int minSelect = 0;
    @Min(0) private int maxSelect = 1; // multiSelect=false면 1
}