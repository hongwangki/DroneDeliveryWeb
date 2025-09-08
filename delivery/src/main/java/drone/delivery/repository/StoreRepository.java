package drone.delivery.repository;

import drone.delivery.domain.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByCategory(String category);
    @EntityGraph(attributePaths = "products")
    Optional<Store> findByIdAndMember_Id(Long storeId, Long ownerId);

    @EntityGraph(attributePaths = "products")
    List<Store> findByMember_id(Long ownerId);


    // products까지 한 번에 필요하면
    @EntityGraph(attributePaths = "products")
    Optional<Store> findWithProductsByIdAndMember_Id(Long storeId, Long ownerId);

}
