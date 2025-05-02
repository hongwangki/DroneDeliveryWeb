package drone.delivery.order;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주문 생성 테스트
 */
@SpringBootTest
public class createOrderTest {
    @Test
    public void createOrder() {
        //given
        Member member=new Member();
        Order order=new Order();
        Product product=new Product();
        member.setName("member1");

        //when
        product.createProduct("치킨",20000,1);
        order.createOrder(member,product);

        //then
        assertThat(order.getMember().getName()).isEqualTo("member1");
        assertThat(product.getFoodName()).isEqualTo("치킨");
        assertThat(order.getTotalPrice()).isEqualTo(20000);



    }
}
