package drone.delivery.repository;

import drone.delivery.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    Long countByReviewId(Long reviewId);

    // 리뷰 ID로 전체 조회
    List<ReviewImage> findAllByReview_Id(Long reviewId);

    // 리뷰 ID로 일괄 삭제 (삭제된 행 수 반환)
    long deleteByReview_Id(Long reviewId);

    List<ReviewImage> findByReviewId(Long reviewId);
}
