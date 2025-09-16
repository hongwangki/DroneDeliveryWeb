package drone.delivery.repository;

import drone.delivery.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    Long countByReviewId(Long reviewId);
}
