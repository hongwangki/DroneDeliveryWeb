package drone.delivery.controller;

import drone.delivery.CartItem;
import drone.delivery.domain.Product;
import drone.delivery.domain.Store;
import drone.delivery.service.ProductService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/delivery")
public class StoreController {

    private final StoreService storeService;
    private final ProductService productService;

    // 카테고리별 가게 조회
    @GetMapping
    public String showStores(@RequestParam(value = "category", required = false) String category, Model model) {
        List<Store> stores;

        if (category == null || category.equals("전체")) {
            stores = storeService.findAll();
        } else {
            stores = storeService.findByCategory(category);
        }

        model.addAttribute("stores", stores);
        model.addAttribute("selectedCategory", category == null ? "전체" : category);
        return "store-list";
    }

    @GetMapping("/{storeId}")
    public String showStoreMenu(@PathVariable Long storeId, Model model, HttpSession session) {
        Store store = storeService.findById(storeId);
        model.addAttribute("store", store);
        model.addAttribute("products", store.getProducts());

        // 세션에 마지막으로 본 가게 ID 저장
        session.setAttribute("lastStoreId", storeId);

        // 장바구니 처리
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cart", cart);

        int totalPrice = cart.stream().mapToInt(CartItem::getTotalPrice).sum();
        model.addAttribute("totalPrice", totalPrice);

        return "product-list";
    }





}
