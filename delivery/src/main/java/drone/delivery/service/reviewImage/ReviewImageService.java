package drone.delivery.service.reviewImage;

import drone.delivery.domain.Review;
import drone.delivery.domain.ReviewImage;
import drone.delivery.repository.ReviewImageRepository;
import drone.delivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewImageStorage storage;

    @Value("${app.upload.allowed-content-types}")
    private List<String> allowedContentTypes;

    @Value("${app.upload.per-review-max:10}")
    private int perReviewMax;

    @Transactional
    public void addImages(Long reviewId, Long memberId, List<MultipartFile> files) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        // 작성자 본인 확인 (프로젝트의 User/Member 구조에 맞춰 체크)
        if (!review.getMember().getId().equals(memberId)) {
            throw new SecurityException("본인 리뷰에만 이미지를 추가할 수 있습니다.");
        }

        long existing = reviewImageRepository.countByReviewId(reviewId);
        if (existing + files.size() > perReviewMax) {
            throw new IllegalArgumentException("리뷰당 최대 " + perReviewMax + "장까지 업로드할 수 있습니다.");
        }

        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;

            String ct = safeContentType(f);
            if (!allowedContentTypes.contains(ct)) {
                throw new IllegalArgumentException("허용되지 않는 파일 형식: " + ct);
            }

            var stored = storage.save(reviewId, f);

            ReviewImage img = ReviewImage.builder()
                    .storedName(stored.storedName())
                    .originalName(truncate(f.getOriginalFilename(), 200))
                    .url(stored.url())
                    .contentType(ct)
                    .size(f.getSize())
                    .review(review)
                    .build();

            review.addImage(img);
        }
        // JPA cascade로 저장됨 (review가 영속 상태인 경우)
    }

    private String safeContentType(MultipartFile f) {
        String ct = f.getContentType();
        if (ct == null) ct = "application/octet-stream";
        return ct.toLowerCase();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return (s.length() <= max) ? s : s.substring(0, max);
    }
}
