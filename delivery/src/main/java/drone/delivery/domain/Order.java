package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToOne
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();




    private int totalPrice;  // 주문 전체 금액
    private String summary;  // "짜장면 x 2, 짬뽕 x 1" 형식

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    //주문 생성
    public static Order createOrder(Member member, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());

        int total = 0;
        StringBuilder summary = new StringBuilder();

        for (OrderItem item : orderItems) {
            item.setOrder(order); // 양방향 설정
            order.getOrderItems().add(item);

            total += item.getOrderPrice() * item.getQuantity();

            summary.append("• ")
                    .append(item.getProduct().getFoodName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append("\n");
        }

        order.setTotalPrice(total);
        order.setSummary(summary.toString());

        return order;
    }

}