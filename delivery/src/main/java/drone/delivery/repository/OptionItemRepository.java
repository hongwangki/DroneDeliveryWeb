package drone.delivery.repository;

import drone.delivery.domain.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

}