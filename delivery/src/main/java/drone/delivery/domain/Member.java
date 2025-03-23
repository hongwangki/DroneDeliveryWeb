package drone.delivery.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
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

    LocalDateTime createTime;
    LocalDateTime updateTime;


    private Double latitude;  // 위도
    private Double longitude; // 경도

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    private MemberType memberType;


}
