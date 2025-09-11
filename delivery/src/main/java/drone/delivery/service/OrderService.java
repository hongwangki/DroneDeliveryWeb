package drone.delivery.service;

import drone.delivery.domain.CartItem;
import drone.delivery.domain.*;
import drone.delivery.repository.MemberRepository;
import drone.delivery.repository.OrderRepository;
import drone.delivery.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;


    /**
     * 주문 메서드
     */
    // OrderService
    @Transactional
    public Long order(Member sessionMember, List<CartItem> cart) {

        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        // 1) 회원 로드
        Member member = memberRepository.findById(sessionMember.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));

        // 2) (상품ID -> 요청수량 합계) 맵
        Map<Long, Integer> qtyMap = cart.stream().collect(Collectors.toMap(
                CartItem::getProductId,
                CartItem::getQuantity,
                Integer::sum
        ));

        // 3) 상품들 잠금 로드(경쟁주문 대비)
        List<Product> products = productRepository.findAllByIdInForUpdate(qtyMap.keySet());
        if (products.size() != qtyMap.size()) {
            throw new EntityNotFoundException("일부 상품을 찾을 수 없습니다.");
        }

        // 3-1) 서로 다른 가게 묶음 방지
        Store store = null;
        for (Product p : products) {
            if (store == null) store = p.getStore();
            else if (!store.getId().equals(p.getStore().getId())) {
                throw new IllegalArgumentException("서로 다른 가게의 상품은 한 번에 주문할 수 없습니다.");
            }
        }

        // 4) 합산 수량으로 재고 검증
        for (Product p : products) {
            int requested = qtyMap.getOrDefault(p.getId(), 0);
            if (requested <= 0) throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
            if (p.getQuantity() < requested) {
                throw new IllegalStateException(
                        "재고 부족: " + p.getFoodName() + " (남은 " + p.getQuantity() + "개, 요청 " + requested + "개)"
                );
            }
        }

        // 5) 장바구니 라인 기준으로 가격/주문아이템 생성 (옵션 포함 단가 사용!)
        Map<Long, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, v -> v));

        int totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        StringBuilder summary = new StringBuilder();

        for (CartItem ci : cart) {
            Product product = productById.get(ci.getProductId());
            if (product == null) throw new EntityNotFoundException("상품 없음: id=" + ci.getProductId());

            int reqQty = Math.max(1, ci.getQuantity());
            int unitPrice = Math.max(0, ci.getPrice()); // ✅ 옵션 포함 '단가' 사용

            totalPrice += unitPrice * reqQty;

            OrderItem oi = OrderItem.createOrderItem(product, reqQty, unitPrice);
            orderItems.add(oi);

            summary.append("• ").append(product.getFoodName())
                    .append(" x ").append(reqQty).append("\n");
        }

        // 6) 최소주문가 검증
        if (store != null && store.getMinOrderPrice() > 0 && totalPrice < store.getMinOrderPrice()) {
            throw new IllegalStateException("최소 주문 금액은 " + store.getMinOrderPrice() + "원 입니다.");
        }

        // 7) 잔액 검증
        if (member.getMoney() < totalPrice) {
            throw new IllegalStateException("잔액이 부족합니다. 필요: " + totalPrice + "원");
        }

        // 8) 차감/생성
        for (Product p : products) {
            int requested = qtyMap.getOrDefault(p.getId(), 0);
            p.setQuantity(p.getQuantity() - requested);
        }
        member.setMoney(member.getMoney() - totalPrice);

        Order order = Order.createOrder(member, orderItems);
        order.setSummary(summary.toString());
        order.setTotalPrice(totalPrice);
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        return order.getId();
    }


    //주문 취소 메서드
    @Transactional
    public void cancelOrder(Long orderId) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // 2. 상태 체크
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("이미 배송중인 주문은 취소할 수 없습니다.");
        }

        // 3. 주문 상태 변경
        order.setOrderStatus(OrderStatus.CANCELED);

        // 4. 회원 환불 처리
        Member member = order.getMember();
        member.setMoney(member.getMoney() + order.getTotalPrice()); // 변경 감지로 자동 업데이트

        // 5. 재고 복구 (옵션)
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity()); // 재고 원복
        }

        // 트랜잭션 종료 시점에 DB 반영
    }


    //주문 검색 메서드
    public Long findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("주문이 없습니다. id=" + id))
                .getId();
    }

    //주문 전체 검색 메서드
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    /// //////////////////
    @Transactional
    public Order placeOrder(Member sessionMember, List<CartItem> cart) {
        Long id = order(sessionMember, cart);     // 기존 로직 그대로 사용
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("주문이 없습니다. id=" + id));
    }


}
