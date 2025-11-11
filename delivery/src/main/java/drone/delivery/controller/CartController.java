package drone.delivery.controller;

import drone.delivery.async.OrderSendQueue;
import drone.delivery.domain.CartItem;
import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.Product;
import drone.delivery.dto.AddToCartRequestDTO;
import drone.delivery.dto.ProductOptionsDTO;
import drone.delivery.dto.SendInfoDTO;
import drone.delivery.mapper.OrderToSendInfoMapper;
import drone.delivery.repository.OrderRepository;
import drone.delivery.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final ProductService productService;
    private final OrderService orderService;
    private final MemberService memberService;


    private final OrderRepository orderRepository;
    private final WebClient pythonClient;
    private final OrderToSendInfoMapper mapper;

    private final ProductOptionQueryService productOptionQueryService;
    private final CartFactoryService cartFactoryService;

    private final OrderSendQueue orderSendQueue;

    /** 가게별 장바구니 키 생성 */
    private String cartKey(Long storeId) { return "cart_" + storeId; }

    /** 합계 계산 헬퍼 - 항상 (단가 × 수량)로 계산 */
    private int calcTotal(List<CartItem> cart) {
        if (cart == null || cart.isEmpty()) return 0;
        return cart.stream()
                .mapToInt(ci -> Math.max(0, ci.getPrice()) * Math.max(1, ci.getQuantity()))
                .sum();
    }

    /** (레거시) 공통 cart에서 가게ID 추론 */
    private Long inferStoreIdFromCart(List<CartItem> cart) {
        if (cart == null || cart.isEmpty()) return null;
        try {
            Product first = productService.findById(cart.get(0).getProductId());
            if (first != null && first.getStore() != null) return first.getStore().getId();
        } catch (Exception ignore) {}
        return null;
    }

    // =========================================
    // 1) 메뉴 상세 보기 (옵션 트리 포함)
    // URL 패턴은 /delivery/{storeId}/menu/{productId}
    // =========================================
    @GetMapping("/delivery/{storeId}/menu/{productId}")
    public String viewProductDetail(@PathVariable Long storeId,
                                    @PathVariable Long productId,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes ra) {

        // 상품 + 옵션 트리 조회
        ProductOptionsDTO dto;
        try {
            dto = productOptionQueryService.getProductWithOptions(productId);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/delivery/" + storeId;
        }

        // 화면에 필요한 값 세팅 (사이드바용 세션 장바구니 꺼내기)
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(cartKey(storeId));

        model.addAttribute("storeId", storeId);
        model.addAttribute("product", dto);      // 상세 화면용 DTO
        model.addAttribute("cart", cart);        // 우측 장바구니
        model.addAttribute("totalPrice", calcTotal(cart));

        // 로그인/잔액/최소주문금액 등은 기존처럼 필요시 추가
        return "product-detail"; // ← Thymeleaf 템플릿명 (resources/templates/product-detail.html)
    }

    // =========================================
    // 2) 장바구니 담기 (옵션 포함)
    // 기존 /cart/add 를 DTO로 교체
    // =========================================
    @PostMapping("/cart/add")
    public String addToCart(@ModelAttribute AddToCartRequestDTO req,
                            @RequestParam(required = false) Long storeId,
                            HttpSession session,
                            RedirectAttributes ra) {

        // 상세 페이지에서 hidden으로 storeId를 못 받았을 때 대비
        if (storeId == null) {
            try {
                Product p = productService.findById(req.getProductId());
                storeId = p.getStore().getId();
            } catch (Exception ignore) {}
        }

        try {
            // 옵션 검증 + 스냅샷 (여기서 필수 미선택 시 예외 발생)
            CartItem cartItem = cartFactoryService.buildCartItem(req);

            // === 기존 장바구니 추가 로직 그대로 ===
            String key = "cart_" + storeId;
            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute(key);
            if (cart == null) cart = new ArrayList<>();
            cart.add(cartItem);
            session.setAttribute(key, cart);
            session.setAttribute("totalPrice", cart.stream().mapToInt(ci -> ci.getPrice() * ci.getQuantity()).sum());
            session.setAttribute("lastStoreId", storeId);
            session.removeAttribute("cart");

            String msg = cartItem.getProductName() + "이(가) 장바구니에 담겼습니다.";
            ra.addFlashAttribute("success", msg);
            ra.addFlashAttribute("successMessage", msg);
            return "redirect:/delivery/" + storeId;

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 사용자 친화적 메시지로 상세 페이지로 되돌리기
            ra.addFlashAttribute("errorMessage", e.getMessage());
            String back = (storeId != null)
                    ? "/delivery/" + storeId + "/menu/" + req.getProductId()
                    : "/delivery";
            return "redirect:" + back;
        }
    }


    // =========================================
    // 3) 주문 확정 (기존 유지) 실제 주문
    // =========================================
    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam(required = false) Long storeId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        Member sessionMember = (Member) session.getAttribute("loginMember");

        // 가게 정보 확인
        if (storeId == null) {
            Long last = (Long) session.getAttribute("lastStoreId");
            if (last != null) storeId = last;
            if (storeId == null) {
                @SuppressWarnings("unchecked")
                List<CartItem> legacy = (List<CartItem>) session.getAttribute("cart");
                storeId = inferStoreIdFromCart(legacy);
            }
        }

        if (storeId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "가게 정보를 찾을 수 없습니다.");
            return "redirect:/delivery";
        }

        String key = cartKey(storeId);
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(key);

        if (sessionMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/delivery/" + storeId;
        }
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "🛒 장바구니가 비어 있어 주문할 수 없습니다.");
            return "redirect:/delivery/" + storeId;
        }

        int totalAmount = calcTotal(cart);
        session.setAttribute("totalPrice", totalAmount);

        if (totalAmount <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ 결제 금액이 0원입니다. 주문할 수 없습니다.");
            return "redirect:/delivery/" + storeId;
        }

        try {
            // 주문 처리
            Long orderId = orderService.order(sessionMember, cart);
            session.removeAttribute(key);
            session.setAttribute("totalPrice", 0);

            // 로그인된 사용자 정보 갱신
            Member refreshed = memberService.findById(sessionMember.getId());
            session.setAttribute("loginMember", refreshed);

            // 주문 성공 메시지
            redirectAttributes.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다! (주문번호 #" + orderId + ")");

            /**
             * 주문 후 파이썬 측 서버에 필요한 정보 dto로 변환해서 쏴주기
             */
            Order order = orderRepository.findGraphById(orderId)
                    .orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."));

            SendInfoDTO payload = mapper.map(order);

            // Python으로 전송 (동기 보장 필요시 block, 2~3초 타임아웃 권장)
            /*pythonClient.post()
                    .uri("/orders/webhook")   // 최종: http://localhost:8000/orders/webhook
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(3));*/

            // ✅ 드론 웹훅: URI에 orderId 포함 (예: /orders/123/webhook)
            pythonClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/orders/{orderId}/webhook")
                            .build(orderId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(3));

            log.info("드론 서버 연결 성공");
            // ✅ 여기에 추가 👇
            pythonClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/orders/{orderId}/start")
                            .build(orderId))
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(3));

            log.info("드론 서버와 통신 시작");
            // checkout 성공 직후
            session.setAttribute("currentOrderId", orderId);
            return "redirect:/realtime?orderId=" + orderId;


        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/delivery/" + storeId;
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/delivery/" + storeId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "알 수 없는 오류가 발생했습니다. 다시 시도해 주세요.");
            return "redirect:/delivery/" + storeId;
        }
    }



    // =========================================
    // 4) 삭제 (기존 유지, storeId hidden 필요)
    // =========================================
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 @RequestParam Long storeId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String key = cartKey(storeId);

        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(key);

        if (cart != null) {
            cart.removeIf(item -> item.getProductId().equals(productId));
            session.setAttribute(key, cart);
            session.setAttribute("totalPrice", calcTotal(cart));
            redirectAttributes.addFlashAttribute("successMessage", "상품을 장바구니에서 삭제했습니다.");
        } else {
            redirectAttributes.addFlashAttribute("warnMessage", "장바구니가 비어 있습니다.");
        }

        return "redirect:/delivery/" + storeId;
    }
}