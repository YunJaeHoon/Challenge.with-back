package Challenge.with_back.service;

import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.memory.SseEmitterRepository;
import Challenge.with_back.repository.rdbms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService
{
    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;

    @Value("${SSE_EXPIRATION_TIME}")
    private static Long CONNECTION_EXPIRATION_TIME;

    @Value("${SSE_NOTIFICATION_NAME}")
    private static String SSE_NOTIFICATION_NAME;

    // 알림 SSE 연결 생성
    @Transactional
    public SseEmitter createNotificationConnection(User user)
    {
        // SSE 연결 생성
        SseEmitter sseEmitter = new SseEmitter(CONNECTION_EXPIRATION_TIME);

        // SSE 연결 정보 저장
        sseEmitterRepository.save(user.getId(), sseEmitter);

        // SSE 연결이 완료되거나 타임아웃 시, SSE 연결 정보 삭제
        sseEmitter.onCompletion(() -> sseEmitterRepository.deleteByUserId(user.getId()));
        sseEmitter.onTimeout(() -> sseEmitterRepository.deleteByUserId(user.getId()));

        // SSE 연결이 성공적으로 생성되었음을 클라이언트에게 전송
        try {
            sseEmitter.send(SseEmitter.event()
                    .id("id")
                    .name(SSE_NOTIFICATION_NAME)
                    .data("SSE 연결이 성공적으로 생성되었습니다."));
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.NOTIFICATION_CONNECTION_ERROR, null);
        }

        return sseEmitter;
    }
}
