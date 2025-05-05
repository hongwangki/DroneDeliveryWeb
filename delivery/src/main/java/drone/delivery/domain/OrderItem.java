// OrderItem.java
package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class OrderItem {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int orderPrice; // 단가

    private int quantity; // 수량

    public int getTotalPrice() {
        return orderPrice * quantity;
    }

    public static OrderItem createOrderItem(Product product, int quantity, int orderPrice) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setOrderPrice(orderPrice);
        return item;
    }
}
