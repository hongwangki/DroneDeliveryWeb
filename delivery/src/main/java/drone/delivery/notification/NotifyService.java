package drone.delivery.notification;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotifyService {

    // 회원별 연결들
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // 30분 타임아웃 (원하는 값으로)
    private static final long TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(memberId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> remove(memberId, emitter));
        emitter.onTimeout(() -> remove(memberId, emitter));
        emitter.onError(e -> remove(memberId, emitter));

        // 더미/하트비트 전송 (연결 확인)
        try {
            emitter.send(SseEmitter.event().name("ping").data("ok"));
        } catch (IOException ignored) { }

        return emitter;
    }

    private void remove(Long memberId, SseEmitter emitter) {
        Optional.ofNullable(emitters.get(memberId)).ifPresent(set -> {
            set.remove(emitter);
            if (set.isEmpty()) emitters.remove(memberId);
        });
    }

    public void send(Long memberId, NotifyEvent event) {
        Set<SseEmitter> set = emitters.get(memberId);
        if (set == null || set.isEmpty()) return;

        List<SseEmitter> dead = new ArrayList<>();
        set.forEach(em -> {
            try {
                em.send(SseEmitter.event().name("notify").data(event));
            } catch (Exception e) {
                dead.add(em);
            }
        });
        dead.forEach(em -> remove(memberId, em));
    }
}
