package drone.delivery.repository;

import drone.delivery.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 상품 ID 리스트로 상품들을 조회하면서,
     * 해당 레코드들을 PESSIMISTIC_WRITE 락을 걸어 가져온다.
     * - 주로 결제/재고 차감 시 동시에 같은 상품을 수정하는 경우를 막기 위해 사용
     * - DB 수준에서 SELECT ... FOR UPDATE 실행
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id asc")
    List<Product> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);


//    //낙관적 락 사용
//    @Query("select p from Product p where p.id in :ids order by p.id asc")
//    List<Product> findAllByIdIn(@Param("ids") Collection<Long> ids);

    /**
     * 특정 가게(storeId) 안에서 메뉴명이 중복되는지 여부를 검사한다.
     * - 메뉴명은 대소문자를 구분하지 않고 검사 (IgnoreCase)
     * - 신규 상품 등록/수정 시 이름 중복 체크 용도
     */
    boolean existsByStore_IdAndFoodNameIgnoreCase(Long storeId, String foodName);


    /**
     * 상품을 가져올 때 옵션 트리(옵션링크 → 옵션그룹 → 옵션아이템)까지
     * 한 번에 fetch join으로 조회한다.
     * - N+1 문제 방지
     * - distinct를 넣어 중복 결과 제거
     * - 반환: Product 하나 (id로 조회)
     */
    @Query("""
        select distinct p
        from Product p
        left join fetch p.optionGroupLinks l
        left join fetch l.optionGroup g
        left join fetch g.items i
        where p.id = :productId
    """)
    Optional<Product> findWithOptionTree(Long productId);

    /**
     * 단순히 상품만 조회하되, 연관된 Store까지 즉시 로딩(EntityGraph 사용)
     * - optionGroupLinks, optionGroups, optionItems는 로딩하지 않음
     * - 상품 상세가 필요 없고, 가게 정보만 필요한 경우에 적합
     */
    @EntityGraph(attributePaths = {"store"})
    Optional<Product> findById(Long id);

    // 같은 가게 내, 동일 이름(대소문자 무시) 상품이 존재하는지 (현재 상품 제외)
    boolean existsByStore_IdAndFoodNameIgnoreCaseAndIdNot(Long storeId, String foodName, Long excludeProductId);

    //성능 테스트용
    Optional<Product> findTopByStoreIdOrderByIdAsc(Long storeId);
}
