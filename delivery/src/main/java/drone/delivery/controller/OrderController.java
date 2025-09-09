package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.OrderStatus;
import drone.delivery.repository.OrderRepository;
import drone.delivery.service.MemberService;
import drone.delivery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final OrderRepository orderRepository;

    //실제 주문이 이루어진 후 메서드
    @GetMapping("/realtime")
    public String realtime(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "realtime";
    }

    //주문 취소 메서드
    @PostMapping("/orders/cancel/{id}")
    @ResponseBody
    public String cancelOrderAjax(@PathVariable Long id, HttpSession session) {

        // 실제 주문 취소
        orderService.cancelOrder(id);

        // 세션 회원 정보 최신화
        Member sessionMember = (Member) session.getAttribute("loggedInMember");
        if (sessionMember != null) {
            Member updatedMember = memberService.findById(sessionMember.getId());
            session.setAttribute("loggedInMember", updatedMember);
        }

        // JSON 형태로 메시지 반환
        return "{\"message\": \"주문이 취소되었습니다.\", \"redirect\": \"/delivery\"}";
    }


    // 주문 내역
    @GetMapping("/orders")
    public String orderList(HttpSession session,
                            Model model,
                            @RequestParam(name = "status", defaultValue = "ALL") String status) {


        Member sessionMember = (Member) session.getAttribute("loggedInMember");
        if (sessionMember == null) {
            return "redirect:/";
        }

        OrderStatus filter = null;
        try {
            if (!"ALL".equalsIgnoreCase(status)) {
                filter = OrderStatus.valueOf(status.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // 잘못된 값이 오면 ALL로 폴백
            status = "ALL";
        }

        List<Order> orders = (filter == null)
                ? orderService.findByMember(sessionMember)                 // 전체
                : orderService.findByMemberAndOrderStatus(sessionMember, filter); // 상태 필터

        model.addAttribute("orders", orders);
        model.addAttribute("status", status.toUpperCase()); // 탭 활성화용
        return "orders"; // templates/orders.html
    }



}
