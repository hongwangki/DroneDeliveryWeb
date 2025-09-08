package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import drone.delivery.domain.Product;
import drone.delivery.domain.Store;
import drone.delivery.dto.FoodDTO;
import drone.delivery.dto.StoreDTO;
import drone.delivery.service.OwnerService;
import drone.delivery.service.ProductService;
import drone.delivery.service.StoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/owner")
public class OwnerController {
    private final OwnerService ownerService;
    private final StoreService storeService;
    private final ProductService productService;

    @GetMapping
    public String ownerHome(HttpSession session, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        // 로그인 및 권한 확인
        if (loggedInMember == null || loggedInMember.getMemberType() != MemberType.OWNER) {
            return "redirect:/login";
        }

        // 사장님 가게 목록 조회
        List<Store> stores = ownerService.findStoresByOwnerId(loggedInMember.getId());

        model.addAttribute("stores", stores);
        model.addAttribute("ownerName", loggedInMember.getName());

        return "owner/home";
    }

    // 사장님이 특정 가게 상세 화면 보기
    @GetMapping("/stores/{storeId}")
    public String showStore(@PathVariable Long storeId,
                            HttpSession session,
                            Model model) {
        // 로그인/권한 확인
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null || loggedInMember.getMemberType() != MemberType.OWNER) {
            return "redirect:/login";
        }

        // 해당 사장님 소유의 가게인지 확인
        Store store = storeService.findStoreByIdAndOwner(storeId, loggedInMember.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없거나 접근 권한이 없습니다."));

        model.addAttribute("store", store);
        return "owner/store"; // templates/owner/storeDetail.html
    }

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        // 로그인 및 권한 확인
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null || loggedInMember.getMemberType() != MemberType.OWNER) {
            return "redirect:/login";
        }

        // 폼 바인딩용 빈 DTO 추가
        model.addAttribute("storeDTO", new StoreDTO());

        // 뷰 이름 반환 (resources/templates/owner/createStore.html)
        return "owner/createStore";
    }

    @PostMapping("/create")
    public String createStore(@ModelAttribute @Valid StoreDTO storeDTO,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력값을 확인해주세요.");
            return "redirect:/owner/stores/create";
        }

        Member owner = (Member) session.getAttribute("loggedInMember");
        if (owner == null || owner.getMemberType() != MemberType.OWNER) {
            return "redirect:/login";
        }

        Long storeId = storeService.createStore(storeDTO, owner.getId());

        redirectAttributes.addFlashAttribute("message", "가게 생성이 완료되었습니다.");
        return "redirect:/owner";
    }

    // 가게 상품 등록 버튼 누를 시
    @PostMapping("/stores/{storeId}")
    public String add(@PathVariable Long storeId,
                      @ModelAttribute @Valid FoodDTO dto,
                      BindingResult br,
                      HttpSession session,
                      RedirectAttributes ra) {
        Member owner = (Member) session.getAttribute("loggedInMember");
        if (owner == null) {
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
        } catch (IllegalArgumentException e) {
            // 여기로 "이미 등록된 메뉴입니다: ~~" 메시지가 들어옴
            ra.addFlashAttribute("formError", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("formError", "메뉴 추가 중 오류가 발생했습니다.");
        }

        return "redirect:/owner/stores/" + storeId + "#add-menu";
    }

}