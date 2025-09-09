package drone.delivery.dto;

import drone.delivery.domain.MemberType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String confirmPassword;

    @NotNull
    private String street;

    @NotNull
    private String city;

    @NotNull
    private String zipcode;
    private String detailAddress;
    private MemberType memberType;
}
