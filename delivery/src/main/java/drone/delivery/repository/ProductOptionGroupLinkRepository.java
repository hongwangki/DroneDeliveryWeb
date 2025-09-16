package drone.delivery.repository;

import drone.delivery.domain.ProductOptionGroupLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupLinkRepository extends JpaRepository<ProductOptionGroupLink, Long> {
//    List<ProductOptionGroupLink> findByProductIdOrderByDisplayOrderAsc(Long productId);
//
//    boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId);

    /**
     * 특정 상품(productId)과 특정 옵션그룹(optionGroupId)의 연결 엔티티를 찾는다.
     * - Product와 OptionGroup은 다대다 관계라서 ProductOptionGroupLink가 중간에 존재
     * - 상품 안에서 "이 옵션그룹이 연결되어 있는지" 확인하거나,
     *   이미 연결된 링크를 불러올 때 사용
     */
    Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId);


    /**
     * 특정 상품(productId)에 연결된 모든 옵션그룹 링크를 가져오는데,
     * sortOrder 기준으로 오름차순 정렬해서 반환한다.
     * - 상품 화면에 옵션그룹을 노출할 때 순서를 보장하기 위함
     * - ProductOptionGroupLink.displayOrder와 sortOrder가 동기화되어 있기 때문에
     *   구버전 호환이나 정렬 처리 시 유용
     */
    List<ProductOptionGroupLink> findByProductIdOrderBySortOrderAsc(Long productId);
}