package drone.delivery.notification;


import drone.delivery.domain.Member;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping(value = "/sse/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpSession session) {
        Member login = (Member) session.getAttribute("loginMember");
        if (login == null) {
            // 비로그인면 연결 안 열어줌
            return new SseEmitter(0L);
        }
        return notifyService.subscribe(login.getId());
    }
}
