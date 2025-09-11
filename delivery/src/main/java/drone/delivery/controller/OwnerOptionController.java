package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import drone.delivery.domain.ProductOptionGroupLink;
import drone.delivery.dto.OptionGroupForm;
import drone.delivery.dto.OptionItemForm;
import drone.delivery.service.OptionOwnerService;
import drone.delivery.service.ProductService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/owner/stores/{storeId}/products/{productId}/options")
public class OwnerOptionController {

    private final OptionOwnerService optionService;
    private final StoreService storeService;
    private final ProductService productService;

    /** 옵션관리 페이지 */
    @GetMapping
    public String page(@PathVariable Long storeId,
                       @PathVariable Long productId,
                       HttpSession session,
                       Model model,
                       RedirectAttributes ra) {

        Member m = (Member) session.getAttribute("loggedInMember");
        if (m == null || m.getMemberType() != MemberType.OWNER) return "redirect:/login";

        // 권한 체크
        optionService.getOwnerStoreOrThrow(storeId, m.getId());

        model.addAttribute("storeId", storeId);
        model.addAttribute("product", productService.findById(productId));
        List<ProductOptionGroupLink> links = optionService.getLinks(productId);
        model.addAttribute("links", links);

        // 폼 바인딩
        model.addAttribute("groupForm", new OptionGroupForm());
        model.addAttribute("itemForm", new OptionItemForm());

        return "owner/product-options";
    }

    /** 새 옵션그룹 생성 + 이 메뉴에 연결 */
    @PostMapping("/groups")
    public String createGroup(@PathVariable Long storeId,
                              @PathVariable Long productId,
                              @ModelAttribute OptionGroupForm form,
                              @RequestParam(defaultValue = "0") int sortOrder,
                              HttpSession session,
                              RedirectAttributes ra) {
        Member m = (Member) session.getAttribute("loggedInMember");
        Long ownerId = m.getId();

        optionService.createGroupAndAttach(storeId, productId, form, sortOrder, ownerId);
        ra.addFlashAttribute("ok", "옵션그룹이 추가되었습니다.");
        return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options";
    }

    /** 옵션아이템 추가 */
    @PostMapping("/groups/{groupId}/items")
    public String addItem(@PathVariable Long storeId,
                          @PathVariable Long productId,
                          @PathVariable Long groupId,
                          @ModelAttribute OptionItemForm form,
                          HttpSession session,
                          RedirectAttributes ra) {
        Member m = (Member) session.getAttribute("loggedInMember");
        optionService.addItem(storeId, productId, groupId, form, m.getId());
        ra.addFlashAttribute("ok", "옵션이 추가되었습니다.");
        return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options#group-" + groupId;
    }

    /** 링크 정렬 변경 */
    @PostMapping("/links/{linkId}/sort")
    public String sort(@PathVariable Long storeId,
                       @PathVariable Long productId,
                       @PathVariable Long linkId,
                       @RequestParam int sortOrder,
                       HttpSession session,
                       RedirectAttributes ra) {
        Member m = (Member) session.getAttribute("loggedInMember");
        optionService.updateSort(storeId, productId, linkId, sortOrder, m.getId());
        ra.addFlashAttribute("ok", "정렬이 변경되었습니다.");
        return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options";
    }

    /** 그룹/아이템 삭제 */
    @PostMapping("/groups/{groupId}/delete")
    public String deleteGroup(@PathVariable Long storeId,
                              @PathVariable Long productId,
                              @PathVariable Long groupId,
                              HttpSession session,
                              RedirectAttributes ra) {
        Member m = (Member) session.getAttribute("loggedInMember");
        optionService.deleteGroup(storeId, productId, groupId, m.getId());
        ra.addFlashAttribute("ok", "그룹을 삭제했습니다.");
        return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options";
    }

    @PostMapping("/items/{itemId}/delete")
    public String deleteItem(@PathVariable Long storeId,
                             @PathVariable Long productId,
                             @PathVariable Long itemId,
                             HttpSession session,
                             RedirectAttributes ra) {
        Member m = (Member) session.getAttribute("loggedInMember");
        optionService.deleteItem(storeId, productId, itemId, m.getId());
        ra.addFlashAttribute("ok", "옵션을 삭제했습니다.");
        return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options";
    }


    /** 품절처리 */
    @PostMapping("/items/{itemId}/soldout")
    public String setItemSoldOut(@PathVariable Long storeId,
                                 @PathVariable Long productId,
                                 @PathVariable Long itemId,
                                 @RequestParam boolean soldOut,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Member m = (Member) session.getAttribute("loggedInMember");
        if (m == null || m.getMemberType() != MemberType.OWNER) {
            return "redirect:/login";
        }

        optionService.setItemSoldOut(storeId, productId, itemId, soldOut, m.getId());
        ra.addFlashAttribute("ok", soldOut ? "옵션을 품절로 전환했습니다." : "옵션 판매를 재개했습니다.");

        return "redirect:/owner/stores/" + storeId + "/products/" + productId + "/options";
    }

}