package drone.delivery.service;

import drone.delivery.domain.Review;
import drone.delivery.dto.ReviewDto;
import drone.delivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;


    /**
     * 리뷰 생성
     */
    @Transactional
    public Long createReview(ReviewDto reviewDto) {

        Review review = new Review();
        review.setStore(reviewDto.getStore());
        review.setProduct(reviewDto.getProduct());
        review.setCreatedBy(reviewDto.getCreatedBy());
        review.setContent(reviewDto.getContent());
        review.setRating(reviewDto.getRating());
        review.setReviewImages(reviewDto.getReviewImages());

        Review savedReview = reviewRepository.save(review);

        return savedReview.getId();

    }


}
