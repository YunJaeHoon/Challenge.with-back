package Challenge.with_back.repository.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class SseEmitterRepository
{
    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    // SSE 연결 정보 저장
    public void save(Long userId, SseEmitter sseEmitter) {
        emitterMap.put(userId, sseEmitter);
    }

    // SSE 연결 정보를 사용자 ID로 조회
    public Optional<SseEmitter> findByUserId(Long userId) {
        SseEmitter sseEmitter = emitterMap.get(userId);;
        return Optional.ofNullable(sseEmitter);
    }

    // SSE 연결 정보를 사용자 ID로 삭제
    public void deleteByUserId(Long userId) {
        emitterMap.remove(userId);
    }
}
