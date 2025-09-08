package drone.delivery.repository;

import drone.delivery.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);

    // 가게(storeId) 내에서 메뉴명(대소문자 무시) 중복 여부
    boolean existsByStore_IdAndFoodNameIgnoreCase(Long storeId, String foodName);
}
