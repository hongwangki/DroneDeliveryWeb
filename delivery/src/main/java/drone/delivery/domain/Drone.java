package drone.delivery.domain;

import drone.delivery.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

@Entity
@Getter @Setter
public class Drone extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DroneStatus droneStatus;

    private int battery;
    private String currentLocation;

    @OneToOne(mappedBy = "drone",cascade = CascadeType.ALL)
    private Order currentOrder;
}
