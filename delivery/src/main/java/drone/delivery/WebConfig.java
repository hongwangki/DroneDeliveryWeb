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


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로그인 검사 제외 경로(화이트리스트)
        // 필요에 따라 추가/수정하세요.
        List<String> whitelist = List.of(
                "/", "/index",
                "/login", "/logout", "/join",
                "/oauth2/**",
                "/css/**", "/js/**", "/images/**", "/uploads/**",
                "/favicon.ico",
                "/error", "/error/**",
                "/drone/**"
        );

        registry.addInterceptor(new LoginCheckInterceptor(whitelist))
                .order(1)
                .addPathPatterns("/**")                    // 기본적으로 전부
                .excludePathPatterns(
                        "/css/**", "/js/**", "/images/**", "/uploads/**",
                        "/favicon.ico",
                        "/error", "/error/**"              // 에러 페이지는 항상 제외
                );
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
