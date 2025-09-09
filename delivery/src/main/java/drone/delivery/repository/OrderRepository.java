package drone.delivery.repository;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o " +
            "from Order o " +
            "where o.member = :member " +
            "order by o.createdDate desc")
    List<Order> findByMember(@Param("member") Member member);

    @Query("select o " +
            "from Order o " +
            "where o.member = :member and " +
            "o.orderStatus = :status " +
            "order by o.createdDate desc")
    List<Order> findByMemberAndOrder(@Param("member")Member member, @Param("status") OrderStatus status);

    @Query("""
        select distinct o
        from Order o
        left join fetch o.orderItems oi
        left join fetch oi.product p
        left join fetch p.store s
        where o.id = :orderId and o.member.id = :memberId
    """)
    Optional<Order> findByIdWithItemsAndProductAndStore(@Param("memberId") Long memberId,
                                                        @Param("orderId") Long orderId);

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);
}

