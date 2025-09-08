package drone.delivery.controller;

import drone.delivery.domain.CartItem;
import drone.delivery.domain.Member;
import drone.delivery.domain.Product;
import drone.delivery.service.MemberService;
import drone.delivery.service.OrderService;
import drone.delivery.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final ProductService productService;
    private final OrderService orderService;
    private final MemberService memberService;

    //장바구니에 음식을 넣는 로직
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Product product = productService.findById(productId);

        // 세션에서 장바구니 가져오기
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        // 이미 담긴 상품이면 수량만 증가
        Optional<CartItem> existing = cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setProductId(product.getId());
            item.setProductName(product.getFoodName());
            item.setPrice(product.getFoodPrice());
            item.setQuantity(quantity);
            cart.add(item);
        }

        // 세션 저장 + flash 메시지 전달
        int total = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
        session.setAttribute("cart", cart);
        session.setAttribute("totalPrice", total); // 총액 저장
        // 장바구니에 담을 때
        session.setAttribute("lastStoreId", product.getStore().getId());
        redirectAttributes.addFlashAttribute("cart", cart);
        redirectAttributes.addFlashAttribute("success", product.getFoodName() + "이(가) 장바구니에 담겼습니다.");

        return "redirect:/delivery/" + product.getStore().getId();
    }

    //주문 확인 전 실제로 원하는 주문이 맞는지 확인하는 로직
    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {

        Member sessionMember = (Member) session.getAttribute("loggedInMember");
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        // 어떤 가게 페이지로 돌려보낼지 계산
        Long storeId = null;
        try {
            if (cart != null && !cart.isEmpty()) {
                Product first = productService.findById(cart.get(0).getProductId());
                if (first != null && first.getStore() != null) {
                    storeId = first.getStore().getId();
                }
            }
        } catch (Exception ignore) {}
        if (storeId == null) {
            storeId = (Long) session.getAttribute("lastStoreId");
        }

        // 로그인/장바구니 기본 검증 (UI 친화적 선검증)
        if (sessionMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "🛒 장바구니가 비어 있어 주문할 수 없습니다.");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }

        // 총액(클라이언트 계산) 0원 방지 — 실제 검증은 서비스에서 다시 함
        int totalAmount = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
        if (totalAmount <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ 결제 금액이 0원입니다. 주문할 수 없습니다.");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }

        try {
            // 실제 주문/검증/재고차감/잔액차감은 서비스에서 원자적으로 처리
            Long orderId = orderService.order(sessionMember, cart);

            // 세션 장바구니 비우기
            session.removeAttribute("cart");

            // 세션의 회원 정보 잔액 갱신(서비스에서 차감했으므로 DB 기준으로 새로 로드하는 게 안전)
            Member refreshed = memberService.findById(sessionMember.getId());
            session.setAttribute("loggedInMember", refreshed);

            redirectAttributes.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다! (주문번호 #" + orderId + ")");
            return "redirect:/realtime";

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            // 잘못된 요청(수량≤0, 존재하지 않는 상품 등)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        } catch (IllegalStateException e) {
            // 재고 부족/최소주문금액/잔액 부족 등 비즈니스 규칙 위반
            // (서비스에서 "재고 부족: 메뉴명 (남은 n개, 요청 m개)" 같은 메시지를 던지도록 구현)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        } catch (Exception e) {
            // 예기치 못한 오류
            redirectAttributes.addFlashAttribute("errorMessage", "알 수 없는 오류가 발생했습니다. 다시 시도해 주세요.");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        // 리다이렉트용 storeId를 먼저 확보
        Long storeId = null;
        try {
            Product p = productService.findById(productId); // 삭제 대상 상품의 가게
            if (p != null && p.getStore() != null) {
                storeId = p.getStore().getId();
            }
        } catch (Exception ignore) {}
        if (storeId == null) { // fallback
            storeId = (Long) session.getAttribute("lastStoreId");
        }

        // 삭제 수행
        if (cart != null) {
            cart.removeIf(item -> item.getProductId().equals(productId));
            session.setAttribute("cart", cart);

            int total = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
            session.setAttribute("totalPrice", total);

            redirectAttributes.addFlashAttribute("successMessage", "상품을 장바구니에서 삭제했습니다.");
        } else {
            redirectAttributes.addFlashAttribute("warnMessage", "장바구니가 비어 있습니다.");
        }

        //  storeId로 리다이렉트
        return "redirect:/delivery/" + (storeId != null ? storeId : "");
    }
}
