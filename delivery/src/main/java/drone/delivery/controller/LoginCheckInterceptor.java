package drone.delivery.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LoginCheckInterceptor implements HandlerInterceptor {

    private final List<String> whitelist;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public LoginCheckInterceptor(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    private boolean isWhitelisted(String uri) {
        for (String p : whitelist) {
            if (pathMatcher.match(p, uri)) return true;
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 0) 에러 처리 경로는 무조건 패스
        if (uri.equals("/error") || uri.startsWith("/error/")) {
            return true;
        }

        // 1) 화이트리스트는 무조건 패스
        if (isWhitelisted(uri)) {
            return true;
        }

        // 2) "실존하는 컨트롤러 핸들러"가 아닌 경우(= 매핑 없음 or 정적 리소스 등) → 로그인 체크하지 않음
        //    => DispatcherServlet이 알아서 404 처리(없는 URL) 혹은 정적 리소스 처리
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 3) 여기 도달 = 보호된 '실존 경로'
        HttpSession session = request.getSession(false);
        Object loginUser = (session == null) ? null : session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser != null) {
            return true;
        }

        // 4) 미인증 → 로그인 페이지로, 이후 원래 URL로 리다이렉트
        String full = uri;
        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) full += "?" + qs;

        String redirect = "/login?redirect=" + URLEncoder.encode(full, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
        return false;
    }
}
