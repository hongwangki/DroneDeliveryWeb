package drone.delivery.async;

import drone.delivery.dto.SendInfoDTO;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class OrderSendQueue {

    private final WebClient pythonClient;

    // 주문 정보를 임시로 저장할 큐 (thread-safe)
    private final BlockingQueue<SendInfoDTO> queue = new LinkedBlockingQueue<>();

    public int getQueueSize() { return queue.size(); }

    // 상태 조회용 컨트롤러
    @Profile({"local","test"})
    @RestController
    @RequestMapping("/test")
    @RequiredArgsConstructor
    class QueueStatusController {
        private final OrderSendQueue orderSendQueue;
        @GetMapping("/queue-size")
        public int size() { return orderSendQueue.getQueueSize(); }
    }

    // 외부에서 주문 DTO를 큐에 넣는 메서드
    public void enqueue(SendInfoDTO dto) {
        queue.offer(dto);
    }

    // 앱 시작 시 워커 스레드 여러 개 실행
    @PostConstruct
    public void startWorkers() {
        int workerCount = 10; // 동시에 Python 서버로 보낼 스레드 수
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);

        for (int i = 0; i < workerCount; i++) {
            executor.submit(() -> {
                while (true) {
                    try {
                        // 큐에서 하나 꺼내기 (없으면 대기)
                        SendInfoDTO dto = queue.take();

                        pythonClient.post()
                                .uri("/api/v_a0_0_1/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dto)
                                .retrieve()
                                .toBodilessEntity()
                                .block(Duration.ofSeconds(3));

                        log.info("✅ Python 서버로 주문 전송 성공: {}", dto);

                    } catch (Exception e) {
                        log.error("❌ Python 서버 전송 실패, 재시도 예정: {}", e.getMessage());
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    }
                }
            });
        }

        log.info("OrderSendQueue 워커 스레드 {}개 시작 완료", workerCount);
    }
}