package drone.delivery.service.reviewImage;

import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

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
}

