package drone.delivery.controller;

import drone.delivery.domain.Member;
import drone.delivery.service.reviewImage.ReviewImageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewImageController {

    private final ReviewImageService imageService;

    @PostMapping("/{reviewId}/images")
    @ResponseBody
    public ResponseEntity<?> uploadImages(
            @PathVariable Long reviewId,
            @RequestParam("files") List<MultipartFile> files,
            HttpSession session
    ) throws IOException {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        imageService.addImages(reviewId, loginMember.getId(), files);
        return ResponseEntity.ok().body("업로드 완료");
    }
}
