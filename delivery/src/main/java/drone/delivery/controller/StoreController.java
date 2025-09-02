package drone.delivery.controller;

import drone.delivery.domain.CartItem;
import drone.delivery.domain.Store;
import drone.delivery.service.OrderService;
import drone.delivery.service.ProductService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/delivery")
public class StoreController {

    private final StoreService storeService;
    private final ProductService productService;
    private final OrderService orderService;

    // 카테고리별 가게 조회
    @GetMapping
    public String showStores(@RequestParam(value = "category", required = false) String category,
                             Model model, HttpSession session) {
        List<Store> stores;

        if (category == null || category.equals("전체")) {
            stores = storeService.findAll();
        } else {
            stores = storeService.findByCategory(category);
        }

        model.addAttribute("stores", stores);
        model.addAttribute("selectedCategory", category == null ? "전체" : category);

        // 주문 내역 최신화
        model.addAttribute("orders", orderService.findAll());

        // 장바구니 최신화
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.stream().mapToInt(CartItem::getTotalPrice).sum());

        return "store-list";
    }


    @GetMapping("/{storeId}")
    public String showStoreMenu(@PathVariable Long storeId, Model model, HttpSession session) {
        // 가게 정보
        Store store = storeService.findById(storeId);
        model.addAttribute("store", store);
        model.addAttribute("products", store.getProducts());

        // 주문 목록 추가
        model.addAttribute("orders", orderService.findAll());  // 여기가 추가 포인트

        // 장바구니 처리
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cart", cart);

        int totalPrice = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
        model.addAttribute("totalPrice", totalPrice);

        // 마지막 본 가게 저장
        session.setAttribute("lastStoreId", storeId);

        return "product-list";
    }






}
