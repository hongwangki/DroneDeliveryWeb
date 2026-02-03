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
import java.util.Objects;

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

    @Transactional
    public void replaceImages(Long reviewId, Long memberId, List<MultipartFile> files) throws IOException {
        // 0) 대상 리뷰 조회 & 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        if (!review.getMember().getId().equals(memberId)) {
            throw new SecurityException("본인 리뷰만 수정할 수 있습니다.");
        }

        // 1) 기존 이미지 목록 조회
        List<ReviewImage> existingImages = reviewImageRepository.findAllByReview_Id(reviewId);

        // 2) 스토리지에서 기존 파일 삭제 (실패는 로깅 후 계속 진행)
        for (ReviewImage img : existingImages) {
            try {
//                storage.delete(img.getStoredName());
            } catch (Exception ignore) {
                // 필요 시 log.warn("스토리지 삭제 실패: {}", img.getStoredName(), ignore);
            }
        }

        // 3) DB에서 기존 이미지 레코드 일괄 삭제
        reviewImageRepository.deleteByReview_Id(reviewId);

        // (양방향 매핑 편의 메서드가 있다면 관계도 정리)
        // for (ReviewImage img : existingImages) {
        //     review.removeImage(img);
        // }

        // 4) 업로드할 유효 파일만 필터링
        List<MultipartFile> validFiles = (files == null) ? List.of() : files.stream()
                .filter(f -> f != null && !f.isEmpty()
                        && !Objects.requireNonNullElse(f.getOriginalFilename(), "").isBlank())
                .toList();

        // 5) 개수 검증
        if (validFiles.size() > perReviewMax) {
            throw new IllegalArgumentException("리뷰당 최대 " + perReviewMax + "장까지 업로드할 수 있습니다.");
        }

        // 6) 새 파일 저장
        for (MultipartFile f : validFiles) {
            String ct = safeContentType(f);
            if (!allowedContentTypes.contains(ct)) {
                throw new IllegalArgumentException("허용되지 않는 파일 형식: " + ct);
            }

            var stored = storage.save(reviewId, f); // 실제 저장소 업로드

            ReviewImage img = ReviewImage.builder()
                    .storedName(stored.storedName())
                    .originalName(truncate(f.getOriginalFilename(), 200))
                    .url(stored.url())
                    .contentType(ct)
                    .size(f.getSize())
                    .review(review)
                    .build();

            review.addImage(img); // 영속 상태면 cascade로 저장됨
        }

        // (선택) orphanRemoval=true 라면, 위의 deleteByReview_Id 없이
        // review.getImages().clear(); 만으로도 DB 삭제가 가능하지만
        // 스토리지 파일은 반드시 수동 삭제가 필요합니다.
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

    @Transactional
    public void deleteImagesByReviewId(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
        for (ReviewImage img : images) {
            try {
                storage.delete(img.getStoredName()); // 저장소 파일 삭제
            } catch (Exception e) {
//                log.warn("이미지 파일 삭제 실패 (id={}): {}", img.getId(), e.getMessage());
            }
        }
        reviewImageRepository.deleteAll(images);
    }
}
