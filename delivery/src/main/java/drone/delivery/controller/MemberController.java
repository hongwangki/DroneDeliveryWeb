package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.dto.MemberDTO;
import drone.delivery.dto.RegisterRequestDTO;
import drone.delivery.dto.UpdateMemberDTO;
import drone.delivery.repository.MemberRepository;
import drone.delivery.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    // 로그인 화면
    @GetMapping("/")
    public String showLoginPage() {
        return "login"; // login.html을 렌더링
    }

    // 로그인 처리 (로그인 성공 시 홈 화면으로 이동)
    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {

        // 로그인 검증 + Member 객체 가져오기
        Member loggedInMember = memberService.validateLogin(email, password);

        if (loggedInMember != null) {
            // 로그인 성공 시 세션에 저장
            session.setAttribute("loggedInMember", loggedInMember);
            return "redirect:/delivery";  // 홈 화면으로
        }

        // 로그인 실패 시 모델에 에러 메시지 추가
        model.addAttribute("errorMessage", "이메일 또는 비밀번호가 틀렸습니다.");
        return "login";  // 로그인 페이지로 돌아감
    }


    // 메인 화면 (홈 화면)
    @GetMapping("/home")
    public String showHomePage(HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/";  // 로그인되지 않은 사용자라면 로그인 화면으로 리디렉션
        }
        return "home"; // home.html을 렌더링
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션을 무효화하여 로그아웃 처리
        return "redirect:/login?logout=true";    // 로그인 페이지로 리디렉션하면서 logout=true 전달
    }


    // 회원가입 화면
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // register.html을 렌더링
    }

    // 회원가입 처리 (이메일 중복 체크 및 성공/실패 처리)
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequestDTO request, Model model) {
        try {
            memberService.registerMember(request);
            return "redirect:/login?success=true"; // 회원가입 성공 후 로그인 페이지로
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register"; // 실패 시 회원가입 페이지로
        }
    }

    // 로그인 화면 (회원가입 후 로그인 화면으로 리디렉션 시 성공 메시지 전달)
    @GetMapping("/login")
    public String showLoginPage1(@RequestParam(value = "logout", required = false) String logout
                                ,@RequestParam(value = "success", required = false)String success, Model model) {
        if (success != null && success.equals("true")) {
            model.addAttribute("successMessage", "회원가입이 성공했습니다! 로그인해주세요.");
        }
        if (logout != null && logout.equals("true")) {
            model.addAttribute("logoutMessage", "로그아웃이 완료되었습니다.");  // 로그아웃 메시지 전달
        }
        return "login"; // login.html을 렌더링
    }


    //정보보기 페이지
    @GetMapping("/account")
    public String accountInfo(Model model, HttpSession session) {
        // 세션에서 로그인한 사용자 정보를 가져옵니다.
        Member member = (Member) session.getAttribute("loggedInMember");

        if (member == null) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "error";  // 에러 페이지로 리디렉션 또는 메시지 처리
        }

        // 모델에 사용자 정보를 추가하여 HTML로 전달
        MemberDTO memberDTO= new MemberDTO(member);
        model.addAttribute("member", memberDTO);
        return "account";  // account.html 페이지로 이동
    }


    //정보 수정 페이지
    @GetMapping("/account/edit")
    public String editAccountInfo(Model model, HttpSession session) {
        // 세션에서 로그인한 사용자 정보를 가져옵니다.
        Member member = (Member) session.getAttribute("loggedInMember");

        if (member == null) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "error";  // 에러 페이지로 리디렉션
        }

        // 사용자 정보를 모델에 추가하여 수정 폼에 전달
        MemberDTO memberDTO= new MemberDTO(member);
        model.addAttribute("member", memberDTO);

        return "editAccount";  // editAccount.html 페이지로 이동
    }

    @PostMapping("/account/edit")
    public String updateAccountInfo(@ModelAttribute UpdateMemberDTO dto,
                                    HttpSession session,
                                    Model model) {

        // 1. 세션에서 현재 로그인한 회원 가져오기
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "error";
        }

        // 2. DB에 회원 정보 업데이트
        memberService.updateMember(member.getId(), dto);

        // 3. DB에서 최신 회원 정보 다시 가져오기
         Member updatedMember = memberRepository.findById(member.getId())
                                 .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));

        // 4. 세션과 모델에 최신 정보 반영
        session.setAttribute("loggedInMember", updatedMember);

        MemberDTO memberDTO = new MemberDTO(updatedMember);
        model.addAttribute("member", memberDTO);
        model.addAttribute("successMessage", "정보가 성공적으로 수정되었습니다.");

        return "account";
    }



    @GetMapping("/recharge")
    public String showRechargeForm(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loggedInMember");
        MemberDTO memberDTO= new MemberDTO(member);
        model.addAttribute("member", memberDTO);
        return "recharge"; // templates/recharge.html
    }

    @PostMapping("/recharge")
    public String rechargeMoney(@RequestParam int money,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (money < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "충전 금액은 0 이상이어야 합니다.");
            return "redirect:/recharge";
        }

        // 세션에서 로그인한 회원 정보 가져오기
        Member sessionMember = (Member) session.getAttribute("loggedInMember");

        // 충전 로직 위임 (DB 반영)
        memberService.chargeMoney(sessionMember.getId(), money);

        // 최신 정보로 세션 갱신
        Member updated = memberService.findById(sessionMember.getId());
        session.setAttribute("loggedInMember", updated);

        redirectAttributes.addFlashAttribute("successMessage", "충전이 완료되었습니다!");
        return "redirect:/recharge";
    }



}
