package drone.delivery.notification;


import drone.delivery.domain.Order;
import drone.delivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DroneEventHandler {

    private final OrderService orderService;
    private final NotifyService notifyService;

    /** 드론이 완료를 알려왔을 때 호출 (멱등하게 처리) */
    @Transactional
    public void handleDelivered(Long orderId) {
        Optional<Order> opOrder = orderService.findDetail(orderId); // 주문자/멤버 접근용
        Order order = opOrder.get();
        if (order.getOrderStatus() != null && "DELIVERED".equals(order.getOrderStatus().name())) {
            return; // 이미 완료면 재처리 X (멱등)
        }
        Long memberId = order.getMember().getId();
        orderService.markDelivered(memberId, orderId); // ✅ 여기서 DB 상태를 DELIVERED로 전환

        notifyService.send(memberId, NotifyEvent.builder()
                .type("ORDER_DELIVERED")
                .orderId(orderId)
                .message("배달이 완료되었습니다!")
                .build());
    }
}
