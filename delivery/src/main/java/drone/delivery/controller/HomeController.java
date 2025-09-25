package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈버튼 클릭시 어디로 갈지.
 */
@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(HttpSession session) {
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            // 비회원 또는 세션 없는 경우 → 일반 홈(배달 목록)
            return "redirect:/delivery";
        }
        // 예: ROLE이 OWNER(사장님)인지 USER(일반회원)인지로 분기
        if (loginMember.getMemberType()== MemberType.OWNER) {
            return "redirect:/owner"; // 사장님 홈(원하는 URL로)
        }
        return "redirect:/delivery"; // 일반회원 홈
    }
}