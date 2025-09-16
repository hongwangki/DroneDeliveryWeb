package drone.delivery;


import drone.delivery.controller.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir:uploads/reviews}")
    private String reviewUploadBaseDir;

    //화이트리스트
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
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트로 접근 시 항상 /delivery로 보냄
        registry.addViewController("/").setViewName("redirect:/delivery");
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


    private Path resolveUploadsRoot() {
        Path base = Paths.get(reviewUploadBaseDir);
        if (!base.isAbsolute()) {
            base = Paths.get(System.getProperty("user.home")).resolve(base).normalize();
        }
        // base == .../uploads/reviews → 부모 uploads 디렉터리를 리소스 루트로
        Path root = base.getParent() != null ? base.getParent() : base;
        return root;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = resolveUploadsRoot();         // e.g. C:/Users/me/uploads
        String location = root.toUri().toString(); // "file:/C:/Users/me/uploads/"

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
    }

}
