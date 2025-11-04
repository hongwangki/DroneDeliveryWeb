package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.OrderStatus;
import drone.delivery.repository.MemberRepository;
import drone.delivery.service.MemberService;
import drone.delivery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    //실제 주문이 이루어진 후 메서드
    @GetMapping("/realtime")
    public String realtime(
            @RequestParam(required = false) Long orderId,
            HttpSession session,
            Model model
    ) {
        // 1) URL 파라미터가 최우선
        if (orderId == null) {
            // 2) 세션에 저장된 최근 주문
            log.info("order 없음");
            orderId = (Long) session.getAttribute("currentOrderId");
        }
        if (orderId == null) {
            // 3) 회원의 최근 주문으로 복원 (없으면 안내)
            Member login = (Member) session.getAttribute("loginMember");
            if (login != null) {
                orderId = orderService.findLatestOrderIdByMember(login.getId()).orElse(null);
            }
        }
        if (orderId == null) {
            // 아무것도 못 찾으면 빈 화면 + 안내
            model.addAttribute("orderId", null);
            model.addAttribute("order", null);
            return "realtime";
        }

        // 정상 흐름: 상세 조회
        Order order = orderService.findDetail(orderId).get();
        log.info(order.toString());
        // 세션에 “현재 주문” 저장 (재진입 대비)
        session.setAttribute("currentOrderId", orderId);

        model.addAttribute("orderId", orderId);
        model.addAttribute("order", order);
        return "realtime";
    }


    //주문 취소 메서드
    @PostMapping("/orders/cancel/{id}")
    @ResponseBody
    public String cancelOrderAjax(@PathVariable Long id, HttpSession session) {

        // 실제 주문 취소
        orderService.cancelOrder(id);

        // 세션 회원 정보 최신화
        Member sessionMember = (Member) session.getAttribute("loginMember");
        if (sessionMember != null) {
            Member updatedMember = memberService.findById(sessionMember.getId());
            session.setAttribute("loginMember", updatedMember);
        }

        // JSON 형태로 메시지 반환
        return "{\"message\": \"주문이 취소되었습니다.\", \"redirect\": \"/delivery\"}";
    }

    @PostMapping("/test")
    public ResponseEntity<Member> test() {
        Member member= memberService.findById(2L);

        return ResponseEntity.ok(member); // DTO 없이 엔티티 그대로 반환
    }
    // 주문 내역
    @GetMapping("/orders")
    public String orderList(HttpSession session,
                            Model model,
                            @RequestParam(name = "status", defaultValue = "ALL") String status) {


        Member sessionMember = (Member) session.getAttribute("loginMember");
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

    //주문 상세보기
    @GetMapping("/orders/{id}")
    public String orderDetail(HttpSession session,
                              @PathVariable Long id,
                              Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        Long memberId = member.getId();

        Order order = orderService.getDetail(memberId, id); // fetch join으로 아이템/상품/가게까지 로드
        model.addAttribute("order", order);
        return "orders-detail";
    }


    // ttest
    @PostMapping("/orders/deliver/{id}")
    @ResponseBody
    public String markDelivered(@PathVariable Long id,
                                HttpSession session) {
        // 1) 권한/소유자 검증, 상태 전이 허용(PENDING/SHIPPED -> DELIVERED) 체크
        Member member = (Member) session.getAttribute("loginMember");

        orderService.markDelivered(member.getId(), id);

        // 2) 프런트와 맞춘 간단한 JSON 문자열 반환
        return "{\"message\":\"배달 완료 처리되었습니다.\",\"redirect\":\"/realtime\"}";
    }



}


