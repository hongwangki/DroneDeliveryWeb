package drone.delivery.dto;

import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberDTO {
    private String name;
    private String email;
    private Address address; // Address 타입 필드 추가
    private int money;

    // 생성자
    public MemberDTO(Member member) {
        this.name = member.getName();
        this.email = member.getEmail();
        this.address = member.getAddress();
        this.money = member.getMoney();
    }


}

