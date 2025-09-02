package drone.delivery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String street;
    private String city;
    private String zipcode;
    private String detailAddress;
}
