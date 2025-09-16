package drone.delivery.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LoginCheckInterceptor implements HandlerInterceptor {

    private final List<String> whitelist;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public LoginCheckInterceptor(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    private boolean isWhitelisted(String path) {
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) return true;
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) return true;

        HttpSession session = request.getSession(false);
        Object loginUser = (session == null) ? null : session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser != null) return true;

        // redirect 쿼리 구성: 원래 요청 경로 + 쿼리스트링을 그대로 보존
        String full = uri;
        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) full += "?" + qs;

        String redirect = "/login?redirect=" + URLEncoder.encode(full, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
        return false;
    }
}

