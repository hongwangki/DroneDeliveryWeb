package drone.delivery.async;

import drone.delivery.domain.CartItem;
import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.Product;
import drone.delivery.dto.SendInfoDTO;
import drone.delivery.mapper.OrderToSendInfoMapper;
import drone.delivery.repository.MemberRepository;
import drone.delivery.repository.OrderRepository;
import drone.delivery.repository.ProductRepository;
import drone.delivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 동시 주문 처리 속도를 확인하기 위한 컨트롤러 실제 서비스와 관련 x
 */

//@Profile({"local","test"})          // ✅ 운영 차단
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestOrderController {

    private final OrderService orderService;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderToSendInfoMapper mapper;
    private final OrderSendQueue orderSendQueue;

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(@RequestParam Long memberId,
                                              @RequestParam Long storeId,
                                              @RequestParam(defaultValue = "1") Integer qty) {
        // 1) 멤버·상품 확보 (간단하게 첫 상품 사용)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("member not found: " + memberId));
        Product product = productRepository.findTopByStoreIdOrderByIdAsc(storeId)
                .orElseThrow(() -> new IllegalStateException("product not found in store: " + storeId));

        // 2) 장바구니 대용 아이템 구성
        CartItem cartItem = new CartItem();
        cartItem.setProductId(product.getId());
        cartItem.setProductName(product.getFoodName());  // Product에 따라 필드명 다를 수 있음
        cartItem.setPrice(product.getFoodPrice());
        cartItem.setQuantity(qty);      // CartItem은 프로젝트 내 기존 타입 사용
        Long orderId = orderService.order(member, List.of(cartItem));

        // 3) 큐 전송
        log.info("orderId: {}", orderId);
        Order order = orderRepository.findGraphById(orderId)
                .orElseThrow(() -> new IllegalStateException("order not found: " + orderId));
        SendInfoDTO payload = mapper.map(order);
        orderSendQueue.enqueue(payload);

        return ResponseEntity.ok("OK orderId=" + orderId);
    }
}