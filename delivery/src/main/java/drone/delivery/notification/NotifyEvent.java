package drone.delivery.notification;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotifyEvent {
    private String type;      // e.g. "ORDER_DELIVERED"
    private Long orderId;
    private String message;   // e.g. "배달이 완료되었습니다!"
}