package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Product {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;

    private String foodName;
    private int foodPrice;
    private int totalPrice; //food 가격 x 개수
    private int quantity;



    //전체 가격 조회
    public int getTotalPrice() {
        return foodPrice*quantity;
    }

    //상품 주문 메서드
    public Product createProduct(String foodName, int foodPrice, int quantity) {


        this.setFoodName(foodName);
        this.setFoodPrice(foodPrice);
        this.setQuantity(quantity);


        return this;
    }
}
