package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Store {

    @Id @GeneratedValue
    private Long id;

    private String name;          // 식당 이름
    private String description;   // 소개
    private String category;      // 한식/중식/치킨 등
    private String imageUrl;      // 대표 이미지

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    // 연관 관계 메서드
    public void addProduct(Product product) {
        products.add(product);
        product.setStore(this);
    }
}
