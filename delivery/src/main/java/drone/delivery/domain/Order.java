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
    @JoinColumn(name = "member_id") //Member FK
    Member member;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToOne
    @JoinColumn(name = "drone_id")
    private Drone drone;


    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<Product> products=new ArrayList<>();

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    int totalPrice;

    //주문 생성 메서드
    public Order createOrder(Member member, Product... products) {
        this.createTime = LocalDateTime.now();
        this.member = member;
        this.orderStatus = OrderStatus.PENDING;
        this.drone = null; // 추후 드론 설정

        //양방향 설정
        for (Product product : products) {
            product.setOrder(this);
            this.products.add(product);
        }
        return this;
    }


    //주문 총 가격 메서드
    public int getTotalPrice() {
        int sum = 0;
        for (Product product : products) {
            sum += product.getTotalPrice();
        }
        return sum;
    }

}
