package drone.delivery.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter @Setter
public class Address {
    private String street;       // 도로명 주소
    private String city;         // 시
    private String zipcode;      // 우편번호
    private String detailAddress; // 상세 주소

    // 기본 생성자 (JPA에서 사용)
    public Address() {}

    // 생성자 (새로운 필드 포함)
    public Address(String street, String city, String zipcode, String detailAddress) {
        this.street = street;
        this.city = city;
        this.zipcode = zipcode;
        this.detailAddress = detailAddress;
    }
}
