package drone.delivery.repository;

import drone.delivery.domain.Member;
import drone.delivery.domain.Product;
import drone.delivery.domain.Review;
import drone.delivery.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStore(Store store);


    List<Review> findAllByMember(Member member);

    @Query("""
    select distinct r
    from Review r
    left join fetch r.member m
    left join fetch r.order o
    left join fetch r.reviewImages ri
    where r.id = :reviewId
    """)
    Optional<Review> findDetailById(Long reviewId);

    @Query("select r.member.id from Review r where r.id = :reviewId")
    Optional<Long> findAuthorIdById(Long reviewId);
}
