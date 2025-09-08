package drone.delivery.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    private Address address;
    private int money;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Order> orders=new ArrayList<>();

    @OneToMany(mappedBy = "member",cascade = CascadeType.ALL)
    private List<Store> stores=new ArrayList<>();

    private Double latitude;  // 위도
    private Double longitude; // 경도


    @Enumerated(EnumType.STRING)
    private MemberType memberType;


}
