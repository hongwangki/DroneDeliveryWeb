package drone.delivery.repository;

import drone.delivery.domain.Product;
import drone.delivery.domain.Review;
import drone.delivery.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    public List<Review> findByStore(Store store);

    public List<Review> findByProduct(Product product);
}
