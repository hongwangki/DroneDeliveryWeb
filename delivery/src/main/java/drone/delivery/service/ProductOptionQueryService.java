package drone.delivery.service;

import drone.delivery.domain.OptionGroup;
import drone.delivery.domain.OptionItem;
import drone.delivery.domain.Product;
import drone.delivery.domain.ProductOptionGroupLink;
import drone.delivery.dto.ProductOptionsDTO;
import drone.delivery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOptionQueryService {

    private final ProductRepository productRepository;


    /**
     * 상품과 그 상품에 연결된 옵션 그룹/아이템들을 DTO 형태로 가공해 반환한다.
     * - ProductOptionGroupLink.enabled = true 인 링크만 사용
     * - 그룹/아이템 순서는 displayOrder(sortOrder) + id 기준으로 정렬
     * - 혹시 중복 연결된 그룹이 있더라도 1개만 유지
     */
    public ProductOptionsDTO getProductWithOptions(Long productId) {

        // 1. 상품과 옵션 트리(옵션링크→그룹→아이템)를 한 번에 조회 (N+1 방지)
        Product p = productRepository.findWithOptionTree(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // enabled=true만, sortOrder(없으면 0) → id 보조정렬
        List<ProductOptionGroupLink> links = p.getOptionGroupLinks().stream()
                .filter(ProductOptionGroupLink::isEnabled)
                .sorted(Comparator
                        .comparingInt((ProductOptionGroupLink l) -> l.getSortOrder() == null ? 0 : l.getSortOrder())
                        .thenComparing(ProductOptionGroupLink::getId))
                .toList();

        // 같은 그룹이 여러 링크로 중복되지 않게 보호(혹시 모를 중복 방지)
        Map<Long, OptionGroup> unique = new LinkedHashMap<>();
        for (ProductOptionGroupLink l : links) {
            OptionGroup g = l.getOptionGroup();
            if (g != null && g.getId() != null) unique.putIfAbsent(g.getId(), g);
        }

        List<ProductOptionsDTO.GroupDTO> groups = unique.values().stream()
                .map(this::toGroupDTO)
                .toList();

        //최종 ProductOptionsDTO 조립 후 반환
        return ProductOptionsDTO.builder()
                .productId(p.getId())
                .name(p.getFoodName())
                .basePrice(p.getFoodPrice())
                .imageUrl(p.getProductImageUrl())
                .groups(groups)
                .build();
    }


    /**
     * OptionGroup 엔티티를 ProductOptionsDTO.GroupDTO 로 변환한다.
     * - 그룹의 메타데이터(이름, 선택 방식, 최소/최대 선택 수 등)를 DTO에 담는다.
     * - 그룹에 속한 OptionItem들을 정렬(displayOrder → id) 후 ItemDTO로 매핑한다.
     * - 결과적으로 "상품 옵션 그룹 + 그 하위 아이템들"을 표현하는 DTO 하나를 리턴.
     */
    private ProductOptionsDTO.GroupDTO toGroupDTO(OptionGroup g) {
        // 현재 스키마: multiSelect(boolean) 기반
        String selectType = g.isMultiSelect() ? "MULTI" : "SINGLE";

        // 아이템 정렬: sortOrder(없으면 0) → id
        List<ProductOptionsDTO.ItemDTO> items = g.getItems().stream()
                .sorted(Comparator
                        .comparingInt((OptionItem it) -> it.getSortOrder() == null ? 0 : it.getSortOrder())
                        .thenComparing(OptionItem::getId))
                .map(it -> ProductOptionsDTO.ItemDTO.builder()
                        .itemId(it.getId())
                        .name(it.getName())
                        .priceDelta(it.getPriceDelta() == null ? 0 : it.getPriceDelta())
                        .stock(it.getStock())
                        .build())
                .toList();

        return ProductOptionsDTO.GroupDTO.builder()
                .groupId(g.getId())
                .name(g.getName())
                .selectType(selectType)     // 'SINGLE' or 'MULTI'
                .required(g.isRequired())
                .minSelect(g.getMinSelect())
                .maxSelect(g.getMaxSelect())
                .items(items)
                .build();
    }
}