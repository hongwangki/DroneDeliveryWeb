package drone.delivery.controller;

import drone.delivery.domain.CartItem;
import drone.delivery.domain.Member;
import drone.delivery.domain.Review;
import drone.delivery.domain.Store;
import drone.delivery.dto.StoreFavoriteDto;
import drone.delivery.dto.StoreUpdateDTO;
import drone.delivery.service.FavoriteService;
import drone.delivery.service.OrderService;
import drone.delivery.service.ReviewQueryService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;
    private final OrderService orderService;
    private final ReviewQueryService reviewQueryService;
    private final FavoriteService favoriteService;

    // 카테고리별 가게 조회
    @GetMapping("/delivery")
    public String showStores(@RequestParam(required=false) String category,
                             @RequestParam(defaultValue="stores") String tab,
                             @RequestParam(defaultValue="0") int page,
                             @RequestParam(defaultValue="6") int size,
                             Model model, HttpSession session) {

        // 항상 가게 리스트는 준비(카테고리 탭용)
        List<Store> stores = (category == null || category.equals("전체"))
                ? storeService.findAll() : storeService.findByCategory(category);
        model.addAttribute("stores", stores);
        model.addAttribute("selectedCategory", category == null ? "전체" : category);
        model.addAttribute("tab", tab);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // 하트표시용 Set
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Set<Long> favoriteStoreIds = Set.of();
        if (loginMember != null) {
            favoriteStoreIds = favoriteService.getFavoriteStoreIdSetManaged(loginMember.getId());
        }
        model.addAttribute("favoriteStoreIds", favoriteStoreIds);

        // 찜목록 탭이면 즐겨찾기 Store를 Page로
        if ("favorites".equals(tab) && loginMember != null) {
            Page<Store> favStores = favoriteService.getFavoriteStoresAsEntities(loginMember.getId(), page, size);
            model.addAttribute("favStores", favStores);
        }

        // 기타 기존 바인딩 …
        return "store-list";
    }


    @GetMapping("/delivery/{storeId}")
    public String showStoreMenu(@PathVariable Long storeId,
                                @RequestParam(value = "tab", required = false) String tab,
                                Model model, HttpSession session) {
        Store store = storeService.findById(storeId);
        List<Review> reviews = reviewQueryService.getStoreReviews(storeId);

        log.info("store {} reviews loaded = {}", storeId, reviews.size());


        model.addAttribute("store", store);
        model.addAttribute("products", store.getProducts());
        model.addAttribute("orders", orderService.findAll());

        model.addAttribute("reviews", reviews);
        model.addAttribute("activeTab", "review".equalsIgnoreCase(tab) ? "review" : "menu");

        // per-store 세션 키 사용
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart_" + storeId);
        if (cart == null) cart = new ArrayList<>();
        model.addAttribute("cart", cart);

        //  합계: item.price(옵션 포함 단가) * item.quantity
        int totalPrice = cart.stream().mapToInt(ci -> ci.getPrice() * ci.getQuantity()).sum();
        model.addAttribute("totalPrice", totalPrice);

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

    //가게 삭제 메서드
    @PostMapping("/owner/stores/{storeId}/delete")
    public String storeDelete(@PathVariable Long storeId, RedirectAttributes ra) {
        // 팝업을 띄우기 위한 URL 전달 (팝업을 띄우고 사용자 확인 후 삭제)
        ra.addFlashAttribute("confirmationMessage", "정말로 삭제하시겠습니까? 복구할 수 없습니다.");
        storeService.deleteStore(storeId);  // 실제 삭제 실행
        ra.addFlashAttribute("message", "삭제가 완료되었습니다.");
        return "redirect:/owner";
    }




}
