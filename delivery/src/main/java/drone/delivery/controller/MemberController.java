package drone.delivery.controller;

import drone.delivery.domain.Address;
import drone.delivery.domain.Member;
import drone.delivery.domain.MemberType;
import drone.delivery.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

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
        // 로그인 검증
        if (memberService.validateLogin(email, password)) {
            // 로그인 성공 시 사용자 정보 세션에 저장
            Member loggedInMember = memberService.findByEmail(email); // 이메일로 회원을 찾음
            session.setAttribute("loggedInMember", loggedInMember);  // 세션에 회원 정보 저장
            return "redirect:/home";  // 홈 화면으로 리디렉션
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
    public String register(@RequestParam("name") String name,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword,
                           @RequestParam("street") String street,
                           @RequestParam("city") String city,
                           @RequestParam("zipcode") String zipcode,
                           @RequestParam("detailAddress") String detailAddress,
                           Model model) {

        // 비밀번호 확인
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");  // 비밀번호 불일치 시 에러 메시지
            return "register"; // 비밀번호 불일치 시 다시 register 페이지로 돌아감
        }

        // 이름 중복 체크
        if (memberService.isNameExist(name)) {
            model.addAttribute("errorMessage", "이미 존재하는 이름입니다."); // 이름 중복 시 경고 메시지
            return "register"; // 중복된 이름인 경우 다시 회원가입 페이지로 돌아가도록 처리
        }

        // 이메일 중복 체크
        if (memberService.isEmailExist(email)) {
            model.addAttribute("errorMessage", "이미 존재하는 이메일입니다."); // 이메일 중복 시 경고 메시지
            return "register"; // 중복된 이메일인 경우 다시 회원가입 페이지로 돌아가도록 처리
        }

        // 새로운 Member 객체 생성
        Member member = new Member();
        member.setName(name);
        member.setEmail(email);
        member.setPassword(password);

        // Address 객체 설정
        Address address = new Address(street, city, zipcode,detailAddress);
        member.setAddress(address);


        // 기본 타입은 USER
        member.setMemberType(MemberType.USER);


        // 회원가입 처리
        memberService.join(member);

        // 회원가입 후 로그인 페이지로 리디렉션하면서 success 파라미터를 전달
        return "redirect:/login?success=true";  // 로그인 페이지로 리디렉션, success=true 전달
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
        model.addAttribute("member", member);

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
        model.addAttribute("member", member);

        return "editAccount";  // editAccount.html 페이지로 이동
    }

    //정보 수정 페이지
    @PostMapping("/account/edit")
    public String updateAccountInfo(@RequestParam("name") String name,
                                    @RequestParam("email") String email,
                                    @RequestParam("street") String street,
                                    @RequestParam("city") String city,
                                    @RequestParam("zipcode") String zipcode,
                                    @RequestParam("detailAddress") String detailAddress,
                                    @RequestParam("password") String password, // 비밀번호 수정 가능
                                    HttpSession session,
                                    Model model) {
        // 세션에서 로그인한 사용자 정보를 가져옵니다.
        Member member = (Member) session.getAttribute("loggedInMember");
        member.setEmail(email);

        if (member == null) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "error";  // 에러 페이지로 리디렉션
        }



        // 사용자 정보 업데이트 (돈은 수정하지 않음)
        member.setName(name);
        Address address = new Address(street, city, zipcode, detailAddress);
        member.setAddress(address);
        if (!password.isEmpty()) {
            member.setPassword(password); // 비밀번호만 수정
        }

        // 서비스 레이어에서 업데이트 처리
        memberService.updateMember(member.getId(), email, name,member.getPassword(), address, member.getMoney(), member.getLatitude(), member.getLongitude());


        // 수정된 정보를 세션에 다시 저장
        session.setAttribute("loggedInMember", member);

        // 성공 메시지를 모델에 추가
        model.addAttribute("successMessage", "정보가 성공적으로 수정되었습니다.");

        // 수정된 정보를 모델에 다시 추가하여 페이지에 전달
        model.addAttribute("member", member);

        // 수정된 정보 페이지로 리디렉션
        return "account";  // 수정된 정보를 바로 표시하는 페이지로 리디렉션
    }




}
