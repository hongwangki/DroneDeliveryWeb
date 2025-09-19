package drone.delivery.mapper;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.Store;
import drone.delivery.dto.FoodItemDTO;
import drone.delivery.dto.SendInfoDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderToSendInfoMapper {
    public SendInfoDTO map(Order order) {
        SendInfoDTO dto = new SendInfoDTO();

        // 멤버 위치
        Member m = order.getMember();
        dto.setUserLatitude(m.getLatitude());
        dto.setUserLongitude(m.getLongitude());

        // 스토어 위치 (Product -> Store 경유 또는 Order.store가 있다면 그걸 사용)
        Store store = order.getOrderItems().isEmpty()
                ? null
                : order.getOrderItems().get(0).getProduct().getStore();
        if (store != null) {
            dto.setStoreLatitude(store.getLatitude());
            dto.setStoreLongitude(store.getLongitude());
        }

        // 아이템들
        List<FoodItemDTO> items = order.getOrderItems().stream().map(oi -> {
            FoodItemDTO fi = new FoodItemDTO();
            fi.setFoodName(oi.getProduct().getFoodName());
            fi.setQuantity(oi.getQuantity());
            return fi;
        }).toList();
        dto.setItems(items);

        return dto;
    }
}