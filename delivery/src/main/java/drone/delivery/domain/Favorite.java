package drone.delivery.domain;

import drone.delivery.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;

/**
 * 찜 엔티티
 */
@Entity
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorite_member_store",
                columnNames = {"member_id", "store_id"})
)
@Getter @Setter
public class Favorite extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "store_id")
    private Store store;

    protected Favorite() {}
    private Favorite(Store store, Member member) { this.store = store; this.member = member; }

    public static Favorite create(Member member, Store store) {
        Favorite f = new Favorite(store, member);
        //member.getFavorites().add(f);
        store.getFavorites().add(f);
        return f;
    }
}
