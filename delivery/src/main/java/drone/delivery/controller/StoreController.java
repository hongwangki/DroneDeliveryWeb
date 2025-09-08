package drone.delivery.controller;

import drone.delivery.domain.CartItem;
import drone.delivery.domain.Store;
import drone.delivery.dto.StoreUpdateDTO;
import drone.delivery.service.OrderService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final OrderService orderService;

    // 카테고리별 가게 조회
    @GetMapping("/delivery")
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


    @GetMapping("/delivery/{storeId}")
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

    // 가게 수정 메서드
    @GetMapping("/owner/stores/{storeId}/edit")
    public String storeEditForm(@PathVariable Long storeId, Model model){
        Store store = storeService.findById(storeId);

        // 화면 바인딩 전용 DTO
        StoreUpdateDTO form = new StoreUpdateDTO();
        form.setName(store.getName());
        form.setDescription(store.getDescription());
        form.setCategory(store.getCategory());
        form.setImageUrl(store.getImageUrl());
        form.setMinOrderPrice(store.getMinOrderPrice());

        model.addAttribute("storeId", storeId);
        model.addAttribute("form", form);
        return "owner/store-edit";
    }
    @PostMapping("/owner/stores/{storeId}/edit")
    public String storeEditSubmit(@PathVariable Long storeId,
                                  @ModelAttribute("form") StoreUpdateDTO form,
                                  BindingResult bindingResult,
                                  RedirectAttributes ra,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            // 에러 시에도 동일한 모델 키 유지
            model.addAttribute("storeId", storeId);
            return "owner/store-edit"; //
        }

        storeService.editStore(storeId, form);
        ra.addFlashAttribute("pageMessage", "가게 정보가 저장되었습니다.");
        return "redirect:/owner/stores/" + storeId;
    }





}
