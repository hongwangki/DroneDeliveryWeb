package drone.delivery.controller;

import drone.delivery.domain.*;
import drone.delivery.dto.ReviewCreateForm;
import drone.delivery.dto.ReviewDto;
import drone.delivery.repository.StoreRepository;
import drone.delivery.service.OrderService;
import drone.delivery.service.ReviewQueryService;
import drone.delivery.service.ReviewService;
import drone.delivery.service.StoreService;
import drone.delivery.service.reviewImage.ReviewImageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;
    private final StoreRepository storeRepository;
    private final StoreService storeService;
    private final ReviewQueryService reviewQueryService;

    @GetMapping("/new")
    public String newReview(HttpSession session,
                            @RequestParam Long orderId,
                            Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login?redirect=/reviews/new?orderId=" + orderId;
        }
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
                         @RequestParam(value = "files", required = false) List<MultipartFile> files, // ★ 파일 함께 받기
                         RedirectAttributes ra,
                         Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login?redirect=/reviews/new?orderId=" + form.getOrderId();
        }
        Long memberId = member.getId();

        // 주문/매장 재조회 (뷰 에러시 재표시용 데이터 세팅에도 사용)
        Order order = orderService.findById(form.getOrderId());
        Store store = storeService.findById(form.getStoreId());

        // 서버측에서도 한 번 더 상태 방어 (신뢰 경계)
        String stName = order.getOrderStatus() != null ? order.getOrderStatus().name() : "";
        if (!"DELIVERED".equals(stName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배달완료 주문만 리뷰 작성이 가능합니다.");
        }

        // 유효성 에러 시, 다시 그려질 데이터 반드시 셋팅 (기존 코드 보완)
        if (binding.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("storeName", store != null ? store.getName() : "알 수 없는 매장");
            model.addAttribute("storeId", form.getStoreId());
            return "reviews-new";
        }

        // 1) 리뷰 생성 -> 생성된 reviewId 반환 (★ ReviewService 메서드가 id 반환하도록 권장)
        Long reviewId = reviewService.createReview(
                toDto(store, member, form.getContent(), form.getRating(), order)
        );

        // 2) 이미지 저장 (파일이 있으면)
        if (files != null && files.stream().anyMatch(f -> f != null && !Objects.requireNonNull(f.getOriginalFilename(), "").isBlank())) {
            try {
                reviewImageService.addImages(reviewId, memberId, files);
            } catch (IOException e) {
                // 이미지 저장 실패 시, 리뷰는 이미 생성됨 → 사용자에게 안내하고 리뷰 상세/주문으로 이동
                log.info(e.getMessage());
                ra.addFlashAttribute("message", "리뷰는 등록되었으나 이미지 업로드에 실패했습니다. 다시 시도해주세요.");
                return "redirect:/orders"; // 필요 시 리뷰 상세로 리다이렉트 경로 조정
            } catch (IllegalArgumentException | SecurityException ex) {
                ra.addFlashAttribute("message", "이미지 업로드 실패: " + ex.getMessage());
                return "redirect:/orders";
            }
        }

        ra.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/reviews/" + reviewId;
    }

    // 리뷰 수정 폼 표시
    @GetMapping("/{reviewId}/edit")
    public String editForm(@PathVariable Long reviewId,
                           HttpSession session,
                           Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login?redirect=/reviews/" + reviewId + "/edit";
        }

        log.info("리뷰 수정 진입");

        Review review = reviewQueryService.getDetail(reviewId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }

        // 로그인 사용자가 작성자가 아니면 접근 금지
        if (!review.getMember().getId().equals(member.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 리뷰만 수정할 수 있습니다.");
        }

        // 폼 데이터 세팅
        ReviewCreateForm form = new ReviewCreateForm();
        form.setOrderId(review.getOrder().getId());
        form.setStoreId(review.getStore().getId());
        form.setContent(review.getContent());
        form.setRating(review.getRating());

        model.addAttribute("form", form);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("storeName", review.getStore().getName());
        model.addAttribute("order", review.getOrder());

        return "reviews-edit"; // 같은 폼 재사용
    }

    // 리뷰 수정 처리
    @PostMapping("/{reviewId}/edit")
    public String update(@PathVariable Long reviewId,
                         HttpSession session,
                         @Valid @ModelAttribute("form") ReviewCreateForm form,
                         BindingResult binding,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         RedirectAttributes ra,
                         Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login?redirect=/reviews/" + reviewId + "/edit";
        }

        Review review = reviewQueryService.getDetail(reviewId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }

        if (!review.getMember().getId().equals(member.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 리뷰만 수정할 수 있습니다.");
        }

        if (binding.hasErrors()) {
            model.addAttribute("reviewId", reviewId);
            model.addAttribute("storeName", review.getStore().getName());
            model.addAttribute("order", review.getOrder());
            return "reviews-edit";
        }

        // 리뷰 수정 수행
        reviewService.updateReview(reviewId, form.getContent(), form.getRating());

        // 이미지 재업로드가 있다면 기존 삭제 후 교체 가능
        if (files != null && !files.isEmpty()) {
            try {
                reviewImageService.replaceImages(reviewId, member.getId(), files);
            } catch (IOException e) {
                log.error("이미지 수정 실패", e);
                ra.addFlashAttribute("message", "리뷰 수정은 완료되었으나 이미지 업로드에 실패했습니다.");
                return "redirect:/reviews/" + reviewId;
            }
        }

        ra.addFlashAttribute("message", "리뷰가 수정되었습니다.");
        return "redirect:/reviews/" + reviewId;
    }


    @GetMapping
    public String reviewList(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login?redirect=/reviews";
        }
        model.addAttribute("reviews", reviewService.findAllByMember(member));
        return "review-list";
    }

    @GetMapping("/{id:[0-9]+}")
    public String detail(@PathVariable("id") Long reviewId,
                         HttpSession session,
                         Model model) {
        Member login = (Member) session.getAttribute("loginMember");
        Long loginId = (login != null) ? login.getId() : null;

        Review review = reviewQueryService.getDetail(reviewId);
        boolean owner = (loginId != null) && reviewQueryService.isOwner(reviewId, loginId);

        model.addAttribute("review", review);
        model.addAttribute("owner", owner);
        return "review-detail"; // templates/review-detail.html
    }

    private ReviewDto toDto(Store store, Member member, String content, Integer rating, Order order) {
        ReviewDto dto = new ReviewDto();
        dto.setStore(store);
        dto.setContent(content);
        dto.setMember(member);
        dto.setRating(rating);
        dto.setOrder(order);
        return dto;
    }


    // ✅ 리뷰 삭제 기능 추가
    @PostMapping("/{reviewId}/delete")
    public String delete(@PathVariable Long reviewId,
                         HttpSession session,
                         RedirectAttributes ra) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login?redirect=/reviews/" + reviewId;
        }

        Review review = reviewQueryService.getDetail(reviewId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }

        if (!review.getMember().getId().equals(member.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 리뷰만 삭제할 수 있습니다.");
        }

        try {
            reviewService.deleteReview(reviewId); // ❗ reviewService에 deleteReview(Long id) 메서드 필요
            ra.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("리뷰 삭제 실패", e);
            ra.addFlashAttribute("message", "리뷰 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/reviews";
    }

}
