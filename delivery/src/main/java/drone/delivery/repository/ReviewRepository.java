package drone.delivery.repository;

import drone.delivery.domain.Member;
import drone.delivery.domain.Product;
import drone.delivery.domain.Review;
import drone.delivery.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStore(Store store);


    List<Review> findAllByMember(Member member);
}
