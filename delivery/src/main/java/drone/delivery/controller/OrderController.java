package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.service.MemberService;
import drone.delivery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;

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



}
