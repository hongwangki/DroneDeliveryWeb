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

    private int totalPrice; //food 가격 x 개수
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private Food food;


    public int getTotalPrice() {
        return food.getPrice() * quantity;
    }
}
