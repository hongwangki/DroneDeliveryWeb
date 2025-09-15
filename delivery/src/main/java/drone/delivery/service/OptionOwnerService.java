package drone.delivery.service;

import drone.delivery.domain.*;
import drone.delivery.dto.OptionGroupForm;
import drone.delivery.dto.OptionItemForm;
import drone.delivery.repository.OptionGroupRepository;
import drone.delivery.repository.OptionItemRepository;
import drone.delivery.repository.ProductOptionGroupLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionOwnerService {

    private final ProductService productService;
    private final OptionGroupRepository optionGroupRepo;
    private final OptionItemRepository optionItemRepo;
    private final ProductOptionGroupLinkRepository linkRepo;
    private final StoreService storeService;


    /**
     * (권한 확인) 점주(ownerId)가 소유한 storeId의 가게를 반환.
     * - 존재하지 않거나 소유자가 아니면 400(IllegalArgumentException) 발생.
     * - 이후 모든 옵션 편집 작업에서 공통으로 호출되어 권한을 보장함.
     */
    public Store getOwnerStoreOrThrow(Long storeId, Long ownerId) {
        return storeService.findStoreByIdAndOwner(storeId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없거나 권한이 없습니다."));
    }


    /**
     * (조회) 특정 상품의 옵션그룹 링크들을 정렬(sortOrder ASC)해서 반환.
     * - 화면에서 옵션그룹 노출 순서 보장.
     * - 링크는 '상품-옵션그룹'을 연결하는 엔티티이며, enabled/sortOrder를 가짐.
     */
    public List<ProductOptionGroupLink> getLinks(Long productId) {
        // 레포도 sortOrder 기준 메서드 사용
        return linkRepo.findByProductIdOrderBySortOrderAsc(productId);
    }


    /**
     * (생성+연결) 새 옵션그룹을 만들고, 지정 상품에 링크로 연결.
     * - 점주 권한 확인 → 상품 로딩 → 그룹 생성(단일/다중 선택 규칙 반영) → 저장
     * - 동일 상품-그룹 조합이 이미 있으면 중복 링크 생성 대신 정렬/활성화만 갱신
     * - 반환값: 생성된 그룹 ID
     */
    public Long createGroupAndAttach(Long storeId, Long productId,
                                     OptionGroupForm form, int sortOrder, Long ownerId) {
        getOwnerStoreOrThrow(storeId, ownerId);

        Product product = productService.findById(productId);

        OptionGroup g = new OptionGroup();
        g.setName(form.getName());
        g.setRequired(form.isRequired());

        // 현재 스키마: multiSelect 사용
        g.setMultiSelect(form.isMultiSelect());

        if (!g.isMultiSelect()) { // SINGLE
            g.setMinSelect(0);
            g.setMaxSelect(1);
        } else {                  // MULTI
            g.setMinSelect(form.getMinSelect());
            g.setMaxSelect(Math.max(form.getMinSelect(), form.getMaxSelect()));
        }
        optionGroupRepo.save(g);

        linkRepo.findByProductIdAndOptionGroupId(product.getId(), g.getId())
                .ifPresentOrElse(link -> {
                    link.setSortOrder(sortOrder);
                    link.setEnabled(true);
                }, () -> {
                    ProductOptionGroupLink link = new ProductOptionGroupLink();
                    link.setProduct(product);
                    link.setOptionGroup(g);
                    link.setSortOrder(sortOrder);
                    link.setEnabled(true);
                    linkRepo.save(link);
                });

        return g.getId();
    }


    /**
     * (연결) 기존에 존재하는 옵션그룹을 상품에 연결.
     * - 점주 권한 확인
     * - 동일 링크 존재 시: 정렬/활성화 갱신
     * - 없으면: 링크 새로 생성
     */
    public void attachExistingGroup(Long storeId, Long productId, Long groupId, int sortOrder, Long ownerId) {
        getOwnerStoreOrThrow(storeId, ownerId);

        linkRepo.findByProductIdAndOptionGroupId(productId, groupId)
                .ifPresentOrElse(link -> {
                    link.setSortOrder(sortOrder);
                    link.setEnabled(true);
                }, () -> {
                    Product product = productService.findById(productId);
                    OptionGroup group = optionGroupRepo.findById(groupId)
                            .orElseThrow(() -> new IllegalArgumentException("옵션 그룹을 찾을 수 없습니다."));
                    ProductOptionGroupLink link = new ProductOptionGroupLink();
                    link.setProduct(product);
                    link.setOptionGroup(group);
                    link.setSortOrder(sortOrder);
                    link.setEnabled(true);
                    linkRepo.save(link);
                });
    }


    /**
     * (아이템 추가) 특정 그룹에 옵션아이템을 추가.
     * - 점주 권한 확인
     * - 그룹 존재 확인
     * - priceDelta null → 0 보정, 정렬값 있으면 반영
     * - 반환값: 생성된 아이템 ID
     */
    public Long addItem(Long storeId, Long productId, Long groupId, OptionItemForm form, Long ownerId) {
        getOwnerStoreOrThrow(storeId, ownerId);

        OptionGroup g = optionGroupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("옵션 그룹을 찾을 수 없습니다."));

        OptionItem i = new OptionItem();
        i.setGroup(g);
        i.setName(form.getName());
        i.setPriceDelta(form.getPriceDelta() == null ? 0 : form.getPriceDelta());
        i.setStock(form.getStock());
        if (form.getSortOrder() != null) i.setSortOrder(form.getSortOrder());
        optionItemRepo.save(i);
        return i.getId();
    }


    /**
     * (삭제) 상품에서 특정 옵션그룹 연결을 끊고, 그룹 자체도 삭제.
     * - 점주 권한 확인
     * - 링크가 있으면 링크 삭제
     * - 그룹 엔티티도 삭제(재사용하지 않는다는 정책 가정)
     */
    public void deleteGroup(Long storeId, Long productId, Long groupId, Long ownerId) {
        getOwnerStoreOrThrow(storeId, ownerId);
        linkRepo.findByProductIdAndOptionGroupId(productId, groupId).ifPresent(linkRepo::delete);
        optionGroupRepo.deleteById(groupId);
    }


    /**
     * (삭제) 특정 옵션아이템 삭제.
     * - 점주 권한 확인
     * - 단순히 ID로 삭제 (참조 무결성은 orphanRemoval에 의존하거나 DB FK)
     */
    public void deleteItem(Long storeId, Long productId, Long itemId, Long ownerId) {
        getOwnerStoreOrThrow(storeId, ownerId);
        optionItemRepo.deleteById(itemId);
    }


    /**
     * (정렬 변경) 링크의 sortOrder를 업데이트.
     * - 점주 권한 확인
     * - 링크 존재 확인 후 정렬값 갱신
     */
    public void updateSort(Long storeId, Long productId, Long linkId, int sortOrder, Long ownerId) {
        getOwnerStoreOrThrow(storeId, ownerId);
        ProductOptionGroupLink link = linkRepo.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("링크를 찾을 수 없습니다."));
        link.setSortOrder(sortOrder);
    }


    /**
     * (품절 토글) 옵션아이템의 판매상태를 stock 필드로 표현.
     * - 점주 권한 확인
     * - 아이템 존재 확인
     * - 아이템이 해당 product의 그룹에 실제로 연결되어 있는지 검증(보안/안전)
     * - 정책: stock=0 → 품절, stock=null → 판매중
     */
    @Transactional
    public void setItemSoldOut(Long storeId, Long productId, Long itemId, boolean soldOut, Long ownerId) {
        // 1) 해당 가게 + 소유자 권한 확인
        getOwnerStoreOrThrow(storeId, ownerId);

        // 2) 아이템 로드
        OptionItem item = optionItemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다."));

        // 3) 이 아이템이 요청한 product의 옵션그룹에 연결되어 있는지 검증(안전)
        boolean linked = linkRepo.findByProductIdAndOptionGroupId(productId, item.getGroup().getId()).isPresent();
        if (!linked) {
            throw new IllegalArgumentException("해당 메뉴의 옵션이 아닙니다.");
        }

        // 4) 재고 개념 제거: stock=0 → 품절, stock=null → 판매중
        item.setStock(soldOut ? 0 : null);
    }
}
