package drone.delivery.service.reviewImage;

import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
@Slf4j
@Component
public class ReviewImageStorage {


    private final Path reviewBaseDir; // === .../uploads/reviews (항상 절대경로)

    public ReviewImageStorage(@Value("${app.upload.base-dir}") String baseDir) {
        Path p = Paths.get(baseDir);
        // 상대경로면 user.home 아래로 고정 (톰캣 work 디렉터리 방지)
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.home")).resolve(p).normalize();
        }
        this.reviewBaseDir = p;
    }

    public Stored save(Long reviewId, MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String ext = getExt(file.getOriginalFilename());
        String storedName = uuid + (ext.isEmpty() ? "" : "." + ext);

        Path dir = reviewBaseDir.resolve(String.valueOf(reviewId));
        Files.createDirectories(dir);

        Path target = dir.resolve(storedName);
        // transferTo(target.toFile()) 대신 NIO copy도 OK
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = "/uploads/reviews/" + reviewId + "/" + storedName; // <- 항상 이 URL
        return new Stored(storedName, url);
    }

    private String getExt(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i >= 0 && i < name.length() - 1) ? name.substring(i + 1) : "";
    }

    public record Stored(String storedName, String url) {}

    /** ✅ 이미지 파일 삭제 */
    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) return;

        try {
            // reviewBaseDir 전체를 탐색해서 해당 파일을 찾아 삭제
            // (보통 reviewId 하위에 있으므로 하위 폴더 포함 탐색)
            Files.walk(reviewBaseDir)
                    .filter(path -> path.getFileName().toString().equals(storedName))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            log.info("이미지 파일 삭제 성공: {}", path);
                        } catch (IOException e) {
                            log.warn("이미지 파일 삭제 실패: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("이미지 삭제 중 오류: {}", storedName, e);
        }
    }

    /** 삭제용: 리뷰 폴더 자체 삭제 (선택 사항) */
    public void deleteReviewFolder(Long reviewId) {
        Path dir = reviewBaseDir.resolve(String.valueOf(reviewId));
        if (!Files.exists(dir)) return;

        try {
            Files.walk(dir)
                    .sorted((a, b) -> b.compareTo(a)) // 하위 파일부터 삭제
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("폴더 삭제 실패: {}", path, e);
                        }
                    });
            log.info("리뷰 폴더 삭제 완료: {}", dir);
        } catch (IOException e) {
            log.warn("리뷰 폴더 삭제 오류: {}", dir, e);
        }
    }
}

