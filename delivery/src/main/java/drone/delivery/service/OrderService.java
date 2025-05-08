package drone.delivery.service;

import drone.delivery.CartItem;
import drone.delivery.domain.*;
import drone.delivery.repository.MemberRepository;
import drone.delivery.repository.OrderRepository;
import drone.delivery.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        //회원 검색
        Member member = memberRepository.findById(sessionMember.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));

        //회원에 대한 아이템 리스트
        List<OrderItem> orderItems = new ArrayList<>();
        int totalPrice = 0;
        StringBuilder summary = new StringBuilder();


        //매개 인자로 넘어온 장바구니를 order_item에 넣는 과정
        for (CartItem item : cart) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

            int quantity = item.getQuantity();
            int unitPrice = product.getFoodPrice();
            int itemTotal = unitPrice * quantity;

            OrderItem orderItem = OrderItem.createOrderItem(product, quantity, unitPrice);
            orderItems.add(orderItem);

            summary.append("• ").append(product.getFoodName())
                    .append(" x ").append(quantity).append("\n");

            totalPrice += itemTotal;
        }

        //주문 상품을 order에 저장
        Order order = Order.createOrder(member, orderItems);
        order.setSummary(summary.toString());
        order.setTotalPrice(totalPrice);
        order.setCreateTime(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order); //DB에 order 저장
        member.setMoney(member.getMoney() - totalPrice); //변경감지를 통해 member 보유금액 자동 저장

        //json으로 쏴주기
    }

    //주문 취소 메서드
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        //만약 준비중이 아닌 이미 배송을 시작했다면 취소 불가.
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("이미 처리 중인 주문은 취소할 수 없습니다.");
        }

        order.setOrderStatus(OrderStatus.CANCELED); // 논리 삭제
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
