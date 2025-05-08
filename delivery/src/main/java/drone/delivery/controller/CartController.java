package drone.delivery.controller;

import drone.delivery.CartItem;
import drone.delivery.domain.Member;
import drone.delivery.domain.Product;
import drone.delivery.service.OrderService;
import drone.delivery.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
        redirectAttributes.addFlashAttribute("cart", cart);
        redirectAttributes.addFlashAttribute("success", product.getFoodName() + "이(가) 장바구니에 담겼습니다.");

        return "redirect:/delivery/" + product.getStore().getId();
    }

    //주문 확인 전 실제로 원하는 주문이 맞는지 확인하는 로직
    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        Member member = (Member) session.getAttribute("loggedInMember");
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        Long storeId = (cart != null && !cart.isEmpty())
                ? productService.findById(cart.get(0).getProductId()).getStore().getId()
                : (Long) session.getAttribute("lastStoreId");

        // 로그인/세션 확인
        if (member == null || cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "🛒 장바구니가 비어 있어 주문할 수 없습니다.");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }

        // 0원 체크
        int totalAmount = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
        if (totalAmount <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ 결제 금액이 0원입니다. 주문할 수 없습니다.");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }

        // 잔액 체크
        if (member.getMoney() < totalAmount) {
            redirectAttributes.addFlashAttribute("errorMessage", "💸 보유 금액이 부족합니다!");
            return "redirect:/delivery/" + (storeId != null ? storeId : "");
        }

        orderService.order(member, cart); //실제 주문
        member.setMoney(member.getMoney() - totalAmount);
        session.removeAttribute("cart");

        redirectAttributes.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다!");
        return "redirect:/realtime";
    }


    //장바구니에서 제외하는 로직
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProductId().equals(productId));
            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("cart", cart);
            int total = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
            redirectAttributes.addFlashAttribute("totalPrice", total);
            redirectAttributes.addFlashAttribute("success", "상품이 장바구니에서 제거되었습니다.");
        }
        return "redirect:/delivery/" + productId; // 또는 적절한 storeId로 리디렉션
    }
}
