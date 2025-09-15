package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Store {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // 식당 이름
    private String description;   // 소개
    private String category;      // 한식/중식/치킨 등
    private String imageUrl;      // 대표 이미지
    private Integer minOrderPrice;    //최소 주문 금액


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @Embedded
    private Address address;
    private Double latitude;  // 위도
    private Double longitude; // 경도


    public Store() {}

    //생성자
    public Store(String name, String description, String category, String imageUrl, Integer minOrderPrice, Member member) {

        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.minOrderPrice = minOrderPrice;
        this.member = member;
    }

    // 연관 관계 메서드
    public void addProduct(Product product) {
        products.add(product);
        product.setStore(this);
    }
}
