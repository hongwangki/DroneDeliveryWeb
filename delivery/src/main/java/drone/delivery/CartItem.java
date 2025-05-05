package drone.delivery;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CartItem {
    private Long productId;
    private String productName;
    private int price;
    private int quantity;

    public int getTotalPrice() {
        return price * quantity;
    }
}