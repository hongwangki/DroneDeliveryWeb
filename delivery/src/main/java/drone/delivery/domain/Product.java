package drone.delivery.domain;

import drone.delivery.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Product extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private String foodName;
    private int foodPrice;           // 기본가격 (옵션 전)
    private int quantity;            // 상품 재고(개별 메뉴 재고)

    private String productImageUrl;

    private String productDescription;

//    @Version
//    private Long version; // 낙관적 락 버전 관리용

    /**
     * Product 1: N ProductOptionGroupLink N: 1 OptionGroup 1: N OptionItems
     * 치킨세트 -> 치킨세트, 사이즈 그룹 ->사이즈 그룹 -> 중간 사이즈
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ProductOptionGroupLink> optionGroupLinks = new ArrayList<>();

    // --- 유틸 ---
    /** 전체 가격 = (기본가 + 옵션합계) * 수량 은 장바구니 단계에서 계산 */
    public int getTotalPriceFallback() {
        return foodPrice * Math.max(1, quantity);
    }

    public static Product createProduct(String foodName, int foodPrice, int quantity) {
        Product product = new Product();
        product.setFoodName(foodName);
        product.setFoodPrice(foodPrice);
        product.setQuantity(quantity);
        return product;
    }

    //init data 삽입용
    public static Product initCreateProduct(String foodName, Integer foodPrice, Integer quantity, String productImageUrl) {
        Product product = new Product();
        product.foodName = foodName;
        product.foodPrice = foodPrice;
        product.quantity = quantity;
        product.productImageUrl = productImageUrl;
        return product;
    }
}