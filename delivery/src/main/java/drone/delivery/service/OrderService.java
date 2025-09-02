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
    public void order(Member sessionMember, List<CartItem> cart) {
        // 회원 조회
        Member member = memberRepository.findById(sessionMember.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));

        List<OrderItem> orderItems = new ArrayList<>();
        int totalPrice = 0;
        StringBuilder summary = new StringBuilder();

        for (CartItem item : cart) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

            int quantity = item.getQuantity();
            int unitPrice = product.getFoodPrice();
            int itemTotal = unitPrice * quantity;

            // 주문 아이템 생성
            OrderItem orderItem = OrderItem.createOrderItem(product, quantity, unitPrice);
            orderItems.add(orderItem);

            // 상품 수량 감소
            product.setQuantity(product.getQuantity() - quantity); // 여기서 재고 감소

            summary.append("• ").append(product.getFoodName())
                    .append(" x ").append(quantity).append("\n");

            totalPrice += itemTotal;
        }

        // 주문 생성
        Order order = Order.createOrder(member, orderItems);
        order.setSummary(summary.toString());
        order.setTotalPrice(totalPrice);
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order); // DB에 저장
        member.setMoney(member.getMoney() - totalPrice); // 금액 차감
        //json으로 쏴주기
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




}
