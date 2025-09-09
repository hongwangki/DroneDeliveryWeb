package drone.delivery.repository;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
