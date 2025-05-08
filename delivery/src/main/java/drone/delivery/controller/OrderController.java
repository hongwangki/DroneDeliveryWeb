package drone.delivery.controller;

import drone.delivery.domain.Order;
import drone.delivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    //실제 주문이 이루어진 후 메서드
    @GetMapping("/realtime")
    public String realtime(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "realtime";
    }
}
