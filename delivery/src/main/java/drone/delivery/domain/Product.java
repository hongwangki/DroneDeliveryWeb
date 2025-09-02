package drone.delivery.domain;

import drone.delivery.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Product extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private String foodName;
    private int foodPrice;
    private int totalPrice; //food 가격 x 개수
    private int quantity;



    //전체 가격 조회
    public int getTotalPrice() {
        return foodPrice*quantity;
    }

    //상품 주문 메서드
    public static Product createProduct(String foodName, int foodPrice, int quantity) {

        Product product = new Product();

        product.setFoodName(foodName);
        product.setFoodPrice(foodPrice);
        product.setQuantity(quantity);


        return product;
    }
}
