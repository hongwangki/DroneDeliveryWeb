//package drone.delivery.order;
//
//import drone.delivery.domain.Member;
//import drone.delivery.domain.Order;
//import drone.delivery.domain.Product;
//import drone.delivery.repository.MemberRepository;
//import drone.delivery.repository.OrderRepository;
//import drone.delivery.repository.ProductRepository;
//import drone.delivery.service.OrderService;
//import drone.delivery.service.ProductService;
//import jakarta.transaction.Transactional;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//@Transactional
//public class OrderTest {
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private MemberRepository memberRepository;
//    @Autowired
//    private ProductRepository productRepository;
//
//
//    @Test
//    @DisplayName("주문 생성 테스트 성공")
//    public void OrderCreation() {
//        // given
//        Member member = new Member();
//        member.setName("John");
//        member.setEmail("john@gmail.com");
//        member.setMoney(100000);
//        memberRepository.save(member);
//
//        Product product = Product.createProduct("치킨", 20000, 10);
//        productRepository.save(product);
//
//        // when: 주문 실행 (order 서비스가 orderId 반환한다고 가정)
//        Long orderId = orderService.order(member.getId(), product.getId(), 2);
//        Order order = orderRepository.findById(orderId).orElseThrow();
//
//        // then: 검증
//        Assertions.assertThat(order.getMember().getName()).isEqualTo("John");
//        Assertions.assertThat(order.getMember().getEmail()).isEqualTo("john@gmail.com");
//        Assertions.assertThat(order.getProducts().size()).isEqualTo(1);
//        Assertions.assertThat(order.getProducts().get(0).getFoodName()).isEqualTo("치킨");
//        Assertions.assertThat(order.getProducts().get(0).getQuantity()).isEqualTo(2);
//        Assertions.assertThat(order.getTotalPrice()).isEqualTo(20000 * 2);
//        Assertions.assertThat(member.getMoney()).isEqualTo(100000 - 40000);
//    }
//}
