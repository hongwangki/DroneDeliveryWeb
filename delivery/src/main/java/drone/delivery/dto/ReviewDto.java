package drone.delivery.dto;

import drone.delivery.domain.Member;
import drone.delivery.domain.Product;
import drone.delivery.domain.ReviewImage;
import drone.delivery.domain.Store;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class ReviewDto {

    @NotNull
    private Store store;

    @NotNull
    private Member createdBy;

    @NotNull
    private Product product;

    @Lob
    @Size(min = 10, max = 500)
    private String content;

    @Max(5)
    @Min(1)
    @NotNull
    private Integer rating;

    private List<ReviewImage> reviewImages;
}
