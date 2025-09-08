package drone.delivery.dto;

import drone.delivery.domain.Member;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreDTO {
    private String name;          // 식당 이름
    private String description;   // 소개
    private String category;      // 한식/중식/치킨 등
    private String imageUrl;      // 대표 이미지
    private Member member;

    public StoreDTO(String name, String description, String category, String imageUrl, Member member, int minOrderPrice) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.member = member;
        this.minOrderPrice = minOrderPrice;
    }
    public StoreDTO(){}
    @Min(0)
    private int minOrderPrice;    //최소 주문 금액
}
