package drone.delivery.domain;

public enum OrderStatus {
    PENDING,     // 주문 접수
    SHIPPED,     // 배송 중
    DELIVERED,   // 배송 완료
    CANCELED,    // 주문 취소
    RETURNED     // 반품 완료
}
