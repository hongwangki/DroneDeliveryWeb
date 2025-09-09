package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.Order;
import drone.delivery.domain.OrderItem;
import drone.delivery.domain.Store;
import drone.delivery.dto.ReviewCreateForm;
import drone.delivery.dto.ReviewDto;
import drone.delivery.repository.StoreRepository;
import drone.delivery.service.OrderService;
import drone.delivery.service.ReviewService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final StoreRepository storeRepository;
    private final StoreService storeService;

    @GetMapping("/new")
    public String newReview(HttpSession session,
                            @RequestParam Long orderId,
                            Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        Long memberId = member.getId();

        // 상세 조회는 N+1 방지를 위해 fetch join 사용 (앞서 만든 메서드 재사용)
        Order order = orderService.getDetail(memberId, orderId);

        String stName = order.getOrderStatus() != null ? order.getOrderStatus().name() : "";
        if (!"DELIVERED".equals(stName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배달완료 주문만 리뷰 작성이 가능합니다.");
        }

        OrderItem firstItem = order.getOrderItems().isEmpty() ? null : order.getOrderItems().get(0);
        Store store = (firstItem != null && firstItem.getProduct() != null) ? firstItem.getProduct().getStore() : null;
        Long storeId = store != null ? store.getId() : null;
        String storeName = store != null ? store.getName() : "알 수 없는 매장";

        ReviewCreateForm form = new ReviewCreateForm();
        form.setOrderId(order.getId());
        form.setStoreId(storeId);

        model.addAttribute("order", order);
        model.addAttribute("storeName", storeName);
        model.addAttribute("storeId", storeId);
        model.addAttribute("form", form);
        return "reviews-new"; // templates/reviews-new.html
    }


    @PostMapping
    public String create(HttpSession session,
                         @Valid @ModelAttribute("form") ReviewCreateForm form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        if (binding.hasErrors()) {
            // 폼 다시 표시를 위해 최소한의 정보 유지
            model.addAttribute("storeName", model.getAttribute("storeName"));
            model.addAttribute("storeId", form.getStoreId());
            return "reviews-new";
        }
        Member member = (Member) session.getAttribute("loggedInMember");
        Long memberId = member.getId();

        Store store = storeService.findById(form.getStoreId());


        reviewService.createReview(
                toDto(store, member, form.getContent(), form.getRating()));


        ra.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/orders";
    }

    @GetMapping
    public String reviewList(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        model.addAttribute("reviews", reviewService.findAllByMember(member));

        return "review-list";

    }

    private ReviewDto toDto(Store store, Member member, String content, Integer rating) {
        ReviewDto dto = new ReviewDto();
        dto.setStore(store);
        dto.setContent(content);
        dto.setCreatedBy(member);
        dto.setRating(rating);
        return dto;
    }

}
