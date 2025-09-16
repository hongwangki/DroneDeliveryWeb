package drone.delivery.service;

import drone.delivery.domain.Member;
import drone.delivery.domain.Review;
import drone.delivery.domain.Store;
import drone.delivery.dto.ReviewDto;
import drone.delivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        review.setMember(reviewDto.getMember());
        review.setContent(reviewDto.getContent());
        review.setRating(reviewDto.getRating());
        review.setOrder(reviewDto.getOrder());


        Review savedReview = reviewRepository.save(review);

        return savedReview.getId();

    }

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> findByStore(Store store) {
        return reviewRepository.findByStore(store);
    }


    public List<Review> findAllByMember(Member member) {
        return reviewRepository.findAllByMember(member);
    }
}
