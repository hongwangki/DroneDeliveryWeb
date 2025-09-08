package drone.delivery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ReviewImage {

    @Id @GeneratedValue
    private Long id;

    private String imagePath;

}
