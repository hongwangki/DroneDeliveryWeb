package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class ReviewImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실제 저장된 파일명(UUID 포함)
    @Column(nullable = false, length = 260)
    private String storedName;

    // 원본 파일명(표시용)
    @Column(nullable = false, length = 200)
    private String originalName;

    // 웹에서 접근하는 URL (/uploads/** 로 매핑)
    @Column(nullable = false, length = 300)
    private String url;

    // MIME type (image/jpeg 등)
    @Column(nullable = false, length = 100)
    private String contentType;

    // 바이트 크기
    @Column(nullable = false)
    private long size;

    // 옵션: 썸네일 URL (생략 가능)
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Review review; // <-- Review 쪽에 mappedBy="reviewImages" 맞춰줄 것

    public void bindReview(Review review) {
        this.review = review;
    }
}