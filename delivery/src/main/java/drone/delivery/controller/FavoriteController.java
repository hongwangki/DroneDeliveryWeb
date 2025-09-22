package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    /** 즐겨찾기 추가 (기존) */
    @PostMapping("/delivery/favorites")
    public String createFavorite(@RequestParam("storeId") Long storeId,
                                 @RequestParam(value = "category", required = false) String category,
                                 HttpSession session) {
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) return "redirect:/login";
        favoriteService.createFavoriteStore(loginMember.getId(), storeId);
        return redirectBackToCategory(category);
    }

    /** 토글(기존, 폴백용) – 리다이렉트 */
    @PostMapping("/delivery/favorites/toggle")
    public String toggleFavorite(@RequestParam Long storeId,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false, defaultValue = "stores") String tab,
                                 @RequestParam(required = false, defaultValue = "0") int page,
                                 @RequestParam(required = false, defaultValue = "6") int size,
                                 HttpSession session) {
        Member m = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (m == null) return "redirect:/login";
        favoriteService.toggleFavoriteAndReturn(m.getId(), storeId); // 내부에서 토글

        if ("favorites".equals(tab)) {
            return "redirect:/delivery?tab=favorites&page=" + page + "&size=" + size;
        }
        if (category == null || category.isBlank() || "전체".equals(category)) return "redirect:/delivery?tab=stores";
        return "redirect:/delivery?tab=stores&category=" + UriUtils.encode(category, StandardCharsets.UTF_8);
    }

    /** AJAX 전용 토글 – 페이지 이동 없이 상태만 반환 */
    @PostMapping("/api/favorites/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavoriteAjax(@RequestParam Long storeId,
                                                                  HttpSession session) {
        Member m = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (m == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        boolean favorited = favoriteService.toggleFavoriteAndReturn(m.getId(), storeId);
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }

    /** 즐겨찾기 삭제(기존) */
    @PostMapping("/delivery/favorites/delete")
    public String deleteFavorite(@RequestParam("storeId") Long storeId,
                                 @RequestParam(value = "category", required = false) String category,
                                 HttpSession session) {
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) return "redirect:/login";
        favoriteService.deleteFavoriteStore(loginMember.getId(), storeId);
        return redirectBackToCategory(category);
    }

    /** 찜목록 탭에서 삭제(기존) */
    @PostMapping("/delivery/favorites/delete-in-tab")
    public String deleteInTab(@RequestParam("storeId") Long storeId,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) return "redirect:/login";
        favoriteService.deleteFavoriteStore(loginMember.getId(), storeId);
        return "redirect:/delivery?tab=favorites&page=" + page + "&size=" + size;
    }

    private String redirectBackToCategory(String category) {
        if (category == null || category.isBlank() || "전체".equals(category)) {
            return "redirect:/delivery";
        }
        return "redirect:/delivery?category=" + UriUtils.encode(category, StandardCharsets.UTF_8);
    }
}
