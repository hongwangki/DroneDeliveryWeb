package drone.delivery.service;

import drone.delivery.domain.Review;
import drone.delivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Review getDetail(Long reviewId) {
        return reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long reviewId, Long memberId) {
        return reviewRepository.findAuthorIdById(reviewId)
                .map(id -> id.equals(memberId))
                .orElse(false);
    }

    public List<Review> getStoreReviews(Long storeId) {
        return reviewRepository.findAllByStoreIdWithMemberAndImages(storeId);
    }
}
