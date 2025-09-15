package drone.delivery.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class CartItem {
    private Long productId;
    private String productName;
    private int price;                 // base price
    private int quantity;              // product quantity
    private List<CartItemOption> options = new ArrayList<>();

    public int getTotalPrice() {
        int optionSum = options.stream()
                .mapToInt(o -> (o.getPriceDelta()) * (o.getQuantity() == null ? 1 : o.getQuantity()))
                .sum();
        return (price + optionSum) * Math.max(1, quantity);
    }
}