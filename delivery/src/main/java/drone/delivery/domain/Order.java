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
}
