package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

@Entity
@Getter @Setter
public class Drone {
    @Id @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private DroneStatus droneStatus;

    private int battery;
    private String currentLocation;

    @OneToOne(mappedBy = "drone",cascade = CascadeType.ALL)
    private Order currentOrder;
}
