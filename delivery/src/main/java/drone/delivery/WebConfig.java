package drone.delivery;


import drone.delivery.controller.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 업로드 베이스(예: uploads). application.yml의 app.upload.base-dir과 맞춰 사용 */
    @Value("${app.upload.base-dir:uploads/reviews}")
    private String reviewUploadBaseDir;

    /** 로그인 체크에서 허용(예외)할 경로 패턴들 */
    private static final List<String> LOGIN_WHITELIST = List.of(
            "/", "/index", "/error", "/favicon.ico",
            // Auth
            "/login", "/join", "/logout", "/register",
            "/users", "/login/**", "/users/oauth/**",
            "/oauth2/**", "/login/oauth2/**",
            // 정적 리소스
            "/css/**", "/js/**", "/images/**", "/webjars/**",
            // 공개 업로드(프로필/리뷰 이미지 등)
            "/uploads/**"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor(LOGIN_WHITELIST))
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(LOGIN_WHITELIST);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 업로드 폴더 노출: /uploads/** → 실제 파일시스템
        // reviewUploadBaseDir: e.g., "uploads/reviews" → 상위 "uploads"를 웹 루트에 매핑
        String uploadsRoot = toUploadsRoot(reviewUploadBaseDir);
        String absolute = Paths.get(uploadsRoot).toAbsolutePath().toString().replace("\\", "/");
        if (!absolute.endsWith("/")) absolute += "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolute)
                .setCacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)).cachePublic());

        // registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");
    }

    private String toUploadsRoot(String reviewBase) {
        // "uploads/reviews" → "uploads"
        if (!StringUtils.hasText(reviewBase)) return "uploads";
        var p = Paths.get(reviewBase).normalize();
        var parent = p.getParent();
        return (parent == null) ? reviewBase : parent.toString();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // (선택) 필요 시 CORS 허용
        registry.addMapping("/**")
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedOrigins("*"); // 운영에서는 구체 도메인으로 제한 권장
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // (선택) 트레일링 슬래시 허용
        configurer.setUseTrailingSlashMatch(true);
    }
}
