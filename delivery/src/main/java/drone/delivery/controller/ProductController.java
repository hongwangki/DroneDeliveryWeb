package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import drone.delivery.domain.Product;
import drone.delivery.dto.FoodDTO;
import drone.delivery.dto.ProductOptionsDTO;
import drone.delivery.repository.ProductRepository;
import drone.delivery.service.ProductOptionQueryService;
import drone.delivery.service.ProductService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Transactional
public class ProductController {
    private final ProductService productService;
    private final ProductOptionQueryService productOptionQueryService;;
    private final ProductRepository productRepository;
    private final StoreService storeService;

    //상품 추가

    @PostMapping("/owner/stores/{storeId}")
    public String add(@PathVariable Long storeId,
                      @ModelAttribute @Valid FoodDTO dto,
                      BindingResult br,
                      @RequestParam(required = false, defaultValue = "false") boolean goConfigure,
                      HttpSession session,
                      RedirectAttributes ra) {

        Member owner = (Member) session.getAttribute("loggedInMember");
        if (owner == null || owner.getMemberType() != MemberType.OWNER) {
            ra.addFlashAttribute("pageError", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        if (br.hasErrors()) {
            ra.addFlashAttribute("formError", "입력값을 확인해주세요.");
            return "redirect:/owner/stores/" + storeId + "#add-menu";
        }

        try {
            Long productId = storeService.addProductToStore(owner.getId(), storeId, dto);
            ra.addFlashAttribute("formMessage", "메뉴가 추가되었습니다.");

            // 체크박스가 켜져 있으면 곧바로 옵션관리 페이지로 이동
            if (goConfigure) {
                return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options";
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("formError", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("formError", "메뉴 추가 중 오류가 발생했습니다.");
        }
        return "redirect:/owner/stores/" + storeId + "#add-menu";
    }

    @GetMapping("/owner/stores/{storeId}/products/{productId}/edit")
    public String editForm(@PathVariable Long storeId,
                           @PathVariable Long productId,
                           Model model) {
        // 기존 상품 불러와서 DTO로 채우기
        Product p = productService.findById(productId);
        FoodDTO dto = FoodDTO.builder()
                .foodName(p.getFoodName())
                .foodPrice(p.getFoodPrice())
                .quantity(p.getQuantity())
                .build();

        // 뷰에서 참조할 키와 동일하게 넣기
        model.addAttribute("foodDTO", dto);
        model.addAttribute("storeId", storeId);
        model.addAttribute("productId", productId);
        return "owner/edit-product";
    }

    @PostMapping("/owner/stores/{storeId}/products/{productId}/edit")
    public String menuEdit(@PathVariable Long storeId,
                           @PathVariable Long productId,
                           @ModelAttribute FoodDTO dto,
                           RedirectAttributes ra) {

        Product product = productService.findById(productId);

        // 🔎 중복 메뉴명 검사 (현재 상품 제외)
        boolean dup = productRepository
                .existsByStore_IdAndFoodNameIgnoreCaseAndIdNot(storeId, dto.getFoodName(), productId);

        if (dup) {
            ra.addFlashAttribute("formError", "이미 등록된 메뉴입니다: " + dto.getFoodName());
            return "redirect:/owner/stores/" + storeId + "#edit-menu";
        }

        // 통과 시 수정
        product.setFoodName(dto.getFoodName().trim());
        product.setFoodPrice(dto.getFoodPrice());
        product.setQuantity(dto.getQuantity());
        product.setProductImageUrl(dto.getProductImageUrl());
        product.setProductDescription(dto.getProductDescription());

        ra.addFlashAttribute("pageMessage", "상품 수정이 완료되었습니다.");
        return "redirect:/owner/stores/" + storeId + "#edit-menu";
    }

    // 메뉴 삭제
    @PostMapping("/owner/stores/{storeId}/products/{productId}/delete")
    public String deleteProduct(@PathVariable Long storeId,
                                @PathVariable Long productId,
                                RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId);

        redirectAttributes.addFlashAttribute("message", "삭제가 완료되었습니다.");
        return "redirect:/owner/stores/" + storeId;
    }



}
