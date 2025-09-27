package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.dto.MemberDTO;
import drone.delivery.dto.RegisterRequestDTO;
import drone.delivery.dto.UpdateMemberDTO;
import drone.delivery.exception.DuplicateEmailException;
import drone.delivery.repository.MemberRepository;
import drone.delivery.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    // 로그인 화면
    @GetMapping("/")
    public String showLoginPage(HttpSession session)
    {
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember != null) return "redirect:/delivery";
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
            session.setAttribute("loginMember", loggedInMember);

            switch (loggedInMember.getMemberType()) {
                case USER:
                    return "redirect:/delivery";
                case OWNER:
                    return "redirect:/owner";
                default:
                    return "redirect:/";
            }
        }


        // 로그인 실패 시 모델에 에러 메시지 추가
        model.addAttribute("errorMessage", "이메일 또는 비밀번호가 틀렸습니다.");
        return "login";  // 로그인 페이지로 돌아감
    }


    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션을 무효화하여 로그아웃 처리
        return "redirect:/login?logout=true";    // 로그인 페이지로 리디렉션하면서 logout=true 전달
    }


    // 회원가입 화면
    @GetMapping("/register")
    public String registerForm(Model model){
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new RegisterRequestDTO());
        }
        return "register";
    }

    // 회원가입 처리 (검증 + 중복/비즈니스 에러 처리)
    @PostMapping("/register")
    public String register(
            @ModelAttribute("request") @Valid RegisterRequestDTO request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        // 1) 비밀번호 확인 일치 여부 (서비스에서도 확인하지만, UX를 위해 컨트롤러에서도 즉시 피드백)
        if (!bindingResult.hasFieldErrors("password")   // 비번 자체 검증 통과한 경우에만
                && !bindingResult.hasFieldErrors("confirmPassword")
                && request.getPassword() != null
                && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "Mismatch", "비밀번호가 일치하지 않습니다.");
        }

        // 2) Bean Validation 에러 있으면 회원가입 페이지로
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "입력값을 확인하세요.");
            // 필요하면 필드별 메시지 맵도 내려줄 수 있음
            // Map<String, String> fieldErrors = bindingResult.getFieldErrors().stream()
            //         .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (a,b)->a));
            // model.addAttribute("fieldErrors", fieldErrors);
            return "register";
        }

        // 3) 비즈니스 로직 실행
        try {
            memberService.registerMember(request);
            ra.addFlashAttribute("successMessage", "회원가입이 완료되었습니다!");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            // 예: 이메일 중복, 이름 중복 등 서비스단 검증 실패
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
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
        Member member = (Member) session.getAttribute("loginMember");

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
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "error";
        }

        // 프로필 카드용
        model.addAttribute("member", new MemberDTO(member));

        // 폼 바인딩용 (UpdateMemberDTO 채우기)
        UpdateMemberDTO form = new UpdateMemberDTO();
        form.setName(member.getName());
        form.setEmail(member.getEmail());
        if (member.getAddress() != null) {
            form.setStreet(member.getAddress().getStreet());
            form.setCity(member.getAddress().getCity());
            form.setZipcode(member.getAddress().getZipcode());
            form.setDetailAddress(member.getAddress().getDetailAddress());
        }
        // 필요 시 money/좌표 등도 미리 세팅
        // form.setMoney(member.getMoney()); ...

        model.addAttribute("updateMemberDTO", form);
        model.addAttribute("page", "account-edit"); // 상단 네비 active 용(선택)

        // 뷰 파일명: editAccount.html (질문 코드 기준)
        return "editAccount";
    }

    @PostMapping("/account/edit")
    public String updateAccountInfo(
            @ModelAttribute("updateMemberDTO") @Valid UpdateMemberDTO dto,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        Member sessionMember = (Member) session.getAttribute("loginMember");
        if (sessionMember == null) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "error";
        }

        // 1) Bean Validation 에러면 폼으로 되돌아가기
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", new MemberDTO(sessionMember)); // 왼쪽 카드 데이터
            model.addAttribute("page", "account-edit");
            return "editAccount";
        }

        try {
            // 2) 비즈니스 업데이트 (중복 이메일 검사 포함)
            memberService.updateMember(sessionMember.getId(), dto);
        } catch (DuplicateEmailException | IllegalStateException e ) {
            // 서비스에서 중복 이메일 예외 던지면 필드 에러로 매핑
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            model.addAttribute("member", new MemberDTO(sessionMember));
            model.addAttribute("page", "account-edit");
            return "editAccount";
        }

        // 3) 최신 회원 재조회 후 세션/모델 반영
        Member updated = memberRepository.findById(sessionMember.getId())
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));
        session.setAttribute("loginMember", updated);

        model.addAttribute("member", new MemberDTO(updated)); // 왼쪽 카드 갱신
        model.addAttribute("updateMemberDTO", dto);           // 사용자가 입력한 값 유지
        model.addAttribute("successMessage", "정보가 성공적으로 수정되었습니다.");
        model.addAttribute("page", "account-edit");

        // 같은 화면에 성공 배지 노출
        return "editAccount";

    }



    @GetMapping("/recharge")
    public String showRechargeForm(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
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
        Member sessionMember = (Member) session.getAttribute("loginMember");

        // 충전 로직 위임 (DB 반영)
        memberService.chargeMoney(sessionMember.getId(), money);

        // 최신 정보로 세션 갱신
        Member updated = memberService.findById(sessionMember.getId());
        session.setAttribute("loginMember", updated);

        redirectAttributes.addFlashAttribute("successMessage", "충전이 완료되었습니다!");
        return "redirect:/recharge";
    }



}
