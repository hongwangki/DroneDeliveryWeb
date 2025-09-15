package drone.delivery.dto;

import drone.delivery.domain.MemberType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {

    @NotNull
    private String name;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식을 입력하세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;

    @NotBlank(message = "도로명 주소는 필수 입력 항목입니다.")
    private String street;

    @NotBlank(message = "도시는 필수 입력 항목입니다.")
    private String city;

    @NotBlank(message = "우편번호는 필수 입력 항목입니다.")
    private String zipcode;

    @NotBlank(message = "상세 주소는 필수 입력 항목입니다.")
    private String detailAddress;

    @NotNull(message = "회원 유형을 선택해야 합니다.")
    private MemberType memberType;
}