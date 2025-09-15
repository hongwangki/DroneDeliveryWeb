package drone.delivery.dto;

import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreDTO {
    @NotBlank
    private String name;          // 식당 이름
    private String description;   // 소개
    private String category;      // 한식/중식/치킨 등
    private String imageUrl;      // 대표 이미지
    private Member member;

    @Min(0)
    private int minOrderPrice;    // 최소 주문 금액

    @Valid
    private Address address;      //  Address 임베디드 DTO 그대로 사용


    public StoreDTO(String name, String description, String category, String imageUrl,
                    Member member, int minOrderPrice, Address address) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.member = member;
        this.minOrderPrice = minOrderPrice;
        this.address = address;
    }

    public StoreDTO() {}
}
