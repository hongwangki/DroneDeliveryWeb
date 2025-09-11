package drone.delivery.service;

import drone.delivery.domain.*;
import drone.delivery.dto.AddToCartRequestDTO;
import drone.delivery.repository.OptionItemRepository;
import drone.delivery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartFactoryService {

    private final ProductRepository productRepository;
    private final OptionItemRepository optionItemRepository;


    /**
     * 요청 DTO를 바탕으로 장바구니(CartItem) 스냅샷을 만든다.
     * - 상품과 옵션트리를 한 번에 로드(findWithOptionTree)하여 N+1 방지
     * - 선택 옵션 유효성 검증(상품-그룹 매칭, 필수/선택 규칙 충족 등)은 validateOptionsAgainstProduct가 담당
     * - 옵션가 합산 + 기본가 → 단가(unitPrice) 계산, 수량 반영하여 totalPrice 계산
     * - 장바구니에 들어가는 건 ‘스냅샷’(이름/가격/옵션명 등)으로 저장
     */
    public CartItem buildCartItem(AddToCartRequestDTO req) {
        // 상품 + 옵션트리 로드
        Product product = productRepository.findWithOptionTree(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 수량 검증
        int quantity = Math.max(1, req.getQuantity() == null ? 1 : req.getQuantity());
        if (product.getQuantity() < 1) {
            throw new IllegalStateException("상품이 품절되었습니다.");
        }

        // 선택된 옵션 id 목록
        List<Long> selectedIds = (req.getOptions() == null) ? List.of() : req.getOptions();

        // 옵션 아이템 일괄 로딩
        Map<Long, OptionItem> itemMap = new HashMap<>();
        if (!selectedIds.isEmpty()) {
            List<OptionItem> items = optionItemRepository.findAllById(selectedIds);
            if (items.size() != selectedIds.size()) {
                throw new IllegalArgumentException("존재하지 않는 옵션이 포함되어 있습니다.");
            }
            for (OptionItem it : items) itemMap.put(it.getId(), it);
        }

        // 검증
        validateOptionsAgainstProduct(product, selectedIds, itemMap);

        //  옵션가 '모든 선택' 합산 (null/언박싱 안전)
        int optionSum = selectedIds.stream()
                .map(itemMap::get)
                .filter(Objects::nonNull)
                .map(OptionItem::getPriceDelta)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        //  단가/총액 계산: 단가 = 기본가 + 옵션합
        int basePrice = Math.max(0, product.getFoodPrice());
        int unitPrice = basePrice + optionSum;
        int totalPrice = unitPrice * quantity;

        // 장바구니 스냅샷
        CartItem cartItem = new CartItem();
        cartItem.setProductId(product.getId());
        cartItem.setProductName(product.getFoodName());
        cartItem.setPrice(unitPrice);     // 옵션 포함 단가
        cartItem.setQuantity(quantity);

        // setTotalPrice(int) 있으면 반영
        if (hasSetter(cartItem, "setTotalPrice")) {
            try {
                cartItem.getClass().getMethod("setTotalPrice", int.class).invoke(cartItem, totalPrice);
            } catch (Exception ignore) {}
        }

        // 옵션 스냅샷
        for (Long id : selectedIds) {
            OptionItem src = itemMap.get(id);
            if (src == null) continue;
            CartItemOption snap = new CartItemOption();
            snap.setOptionItemId(src.getId());
            snap.setName(src.getName());
            snap.setPriceDelta(src.getPriceDelta());
            snap.setQuantity(1);
            cartItem.getOptions().add(snap);
        }

        return cartItem;
    }

    /**
     * 사용자가 고른 옵션들이 해당 상품의 옵션 정책에 맞는지 검증한다.
     * - 상품에 연결된(그리고 enabled=true인) 옵션그룹/아이템만 허용
     * - 품절 옵션 선택 금지
     * - 그룹 규칙(필수/단일/다중 + min/max) 위반 시 예외
     */
    private void validateOptionsAgainstProduct(Product product,
                                               List<Long> selectedIds,
                                               Map<Long, OptionItem> itemMap) {

        // 상품이 보유한(활성) 옵션 그룹 모음
        Map<Long, OptionGroup> groupById = new HashMap<>();
        product.getOptionGroupLinks().stream()
                .filter(ProductOptionGroupLink::isEnabled)
                .map(ProductOptionGroupLink::getOptionGroup)
                .forEach(g -> groupById.put(g.getId(), g));

        // 각 그룹에 속한 옵션 아이템 id 집합
        Map<Long, Set<Long>> groupItemIds = new HashMap<>();
        for (OptionGroup g : groupById.values()) {
            Set<Long> ids = new HashSet<>();
            for (OptionItem it : g.getItems()) ids.add(it.getId());
            groupItemIds.put(g.getId(), ids);
        }

        // 사용자가 고른 아이템들을 그룹별로 분류
        Map<Long, List<OptionItem>> chosenByGroup = new HashMap<>();
        for (Long id : selectedIds) {
            OptionItem item = itemMap.get(id);
            if (item == null) throw new IllegalArgumentException("옵션을 확인할 수 없습니다.");

            OptionGroup g = item.getGroup();

            // 이 상품의 그룹인가?
            if (!groupById.containsKey(g.getId())) {
                throw new IllegalArgumentException("상품에 없는 옵션이 포함되어 있습니다: " + g.getName());
            }
            // 그 그룹의 아이템인가?
            if (!groupItemIds.get(g.getId()).contains(item.getId())) {
                throw new IllegalArgumentException("옵션이 그룹에 속하지 않습니다: " + item.getName());
            }
            // 재고 체크
            if (item.getStock() != null && item.getStock() <= 0) {
                throw new IllegalStateException("옵션 품절: " + item.getName());
            }

            chosenByGroup.computeIfAbsent(g.getId(), k -> new ArrayList<>()).add(item);
        }

        // 그룹 규칙(required / SINGLE / MULTI min,max) 검증
        for (OptionGroup g : groupById.values()) {
            List<OptionItem> chosen = chosenByGroup.getOrDefault(g.getId(), List.of());
            int count = chosen.size();

            if (g.isRequired() && count == 0) {
                throw new IllegalArgumentException("필수 옵션을 선택하세요: " + g.getName());
            }

            SelectType st = g.getSelectType() == null ? SelectType.SINGLE : g.getSelectType();
            switch (st) {
                case SINGLE -> {
                    if (count > 1) {
                        throw new IllegalArgumentException("단일 선택 그룹입니다: " + g.getName());
                    }
                }
                case MULTI -> {
                    Integer min = g.getMinSelect();
                    Integer max = g.getMaxSelect();
                    if (min != null && count < min) {
                        throw new IllegalArgumentException(g.getName() + "은(는) 최소 " + min + "개 선택해야 합니다.");
                    }
                    if (max != null && count > max) {
                        throw new IllegalArgumentException(g.getName() + "은(는) 최대 " + max + "개까지 선택할 수 있습니다.");
                    }
                }
            }
        }
    }

    /** 리플렉션으로 세터 존재 여부 확인 (총액 세터가 있을 때만 호출하려고) */
    private boolean hasSetter(Object obj, String name) {
        try { obj.getClass().getMethod(name, int.class); return true; }
        catch (NoSuchMethodException e) { return false; }
    }
}