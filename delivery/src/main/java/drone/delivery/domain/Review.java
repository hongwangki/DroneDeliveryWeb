package drone.delivery.domain;

import drone.delivery.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Review extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    
    private Order order;
    @Lob
    private String content;

    private Integer rating;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewImage> reviewImages = new ArrayList<>();


    /**
     * 양방향 편의 메소드
     */
    public void addReviewImage(ReviewImage reviewImage) {
        reviewImages.add(reviewImage);
        reviewImage.setReview(this);
    }



}
