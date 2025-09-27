package drone.delivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateMemberDTO {
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 숫자를 포함할 수 없습니다.")
    private String name;
    @Email(message = "올바른 이메일 형식을 입력하세요.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,6}$",
            message = "이메일 도메인 형식이 올바르지 않습니다.")
    private String email;
    private String password;

    @NotBlank(message = "도로명 주소는 필수입니다.")
    private String street;

    @NotBlank(message = "도시는 필수입니다.")
    private String city;

    @NotBlank(message = "우편번호는 필수입니다.")
    // 필요하면 한국 우편번호 패턴 추가: 5자리
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자입니다.")
    private String zipcode;

    @NotBlank(message = "상세 주소는 필수입니다.")
    private String detailAddress;

    private int money;
    private Double latitude;
    private Double longitude;
}
