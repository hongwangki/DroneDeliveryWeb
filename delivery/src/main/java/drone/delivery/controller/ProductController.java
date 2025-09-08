package drone.delivery.controller;

import drone.delivery.domain.Product;
import drone.delivery.dto.FoodDTO;
import drone.delivery.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Transactional
public class ProductController {
    private final ProductService productService;

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

    //상품 수정 메서드
    @PostMapping("/owner/stores/{storeId}/products/{productId}/edit")
    public String menuEdit(@PathVariable Long storeId,@PathVariable Long productId, @ModelAttribute FoodDTO dto,  RedirectAttributes redirectAttributes){
        Product product = productService.findById(productId);
        product.setFoodName(dto.getFoodName());
        product.setFoodPrice(dto.getFoodPrice());
        product.setQuantity(dto.getQuantity());

        redirectAttributes.addFlashAttribute("pageMessage", "상품 수정이 완료되었습니다.");

        return "redirect:/owner/stores/" + storeId + "#edit-menu";
    }


    // 메뉴 삭제
    @PostMapping("/owner/stores/{storeId}/products/{productId}")
    public String deleteProduct(@PathVariable Long storeId,
                                @PathVariable Long productId,
                                RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId);  // 서비스에서 실제 삭제 실행

        redirectAttributes.addFlashAttribute("pageMessage", "삭제가 완료되었습니다.");
        return "redirect:/owner/stores/" + storeId;
    }

}
