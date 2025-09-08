package drone.delivery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateMemberDTO {
    private String name;
    private String email;
    private String password;
    private String street;
    private String city;
    private String zipcode;
    private String detailAddress;
    private int money;
    private Double latitude;
    private Double longitude;
}
