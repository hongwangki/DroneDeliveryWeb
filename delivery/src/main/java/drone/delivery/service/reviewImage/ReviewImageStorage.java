package drone.delivery.service.reviewImage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewImageStorage {

    @Value("${app.upload.base-dir}")
    private String baseDir; // e.g., uploads/reviews

    public Stored save(Long reviewId, MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String ext = getExt(file.getOriginalFilename());
        String storedName = uuid + (ext.isEmpty() ? "" : "." + ext);

        Path dir = Paths.get(baseDir, String.valueOf(reviewId));
        Files.createDirectories(dir);

        Path target = dir.resolve(storedName);
        file.transferTo(target.toFile());

        // 웹에서 접근 가능한 URL (/uploads/**)
        String url = "/uploads/reviews/" + reviewId + "/" + storedName;
        return new Stored(storedName, url);
    }

    private String getExt(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i >= 0 && i < name.length() - 1) ? name.substring(i + 1) : "";
    }

    public record Stored(String storedName, String url) {}
}
