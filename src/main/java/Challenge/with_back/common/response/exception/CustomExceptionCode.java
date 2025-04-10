package Challenge.with_back.common.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // OAuth 2.0 예외
    PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "제공자를 찾을 수 없습니다."),
    INVALID_PROVIDER(HttpStatus.UNAUTHORIZED, "유효하지 않은 제공자입니다."),

    // 계정 관련 예외
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    ALREADY_EXISTING_USER(HttpStatus.CONFLICT, "이미 존재하는 계정입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Refresh token입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "형식에 맞지 않는 비밀번호입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "형식에 맞지 않는 닉네임입니다."),

    // 인증번호 관련 예외
    SEND_EMAIL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 오류가 발생하였습니다."),
    VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "전송된 인증번호가 존재하지 않습니다."),
    CHECK_VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "인증번호를 확인받지 않았습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.GONE, "만료된 인증번호입니다."),
    WRONG_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    TOO_MANY_WRONG_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호를 5회 이상 틀렸습니다."),

    // 알림 예외
    EMITTER_CONNECTION_ERROR(HttpStatus.NOT_FOUND, "SSE Emitter 연결 중 오류가 발생하였습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    EMITTER_NOT_FOUND(HttpStatus.NOT_FOUND, "SSE Emitter를 찾을 수 없습니다."),
    KAFKA_NOTIFICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카프카 알림 수신 중 오류가 발생하였습니다."),

    UNEXPECTED_ERROR(HttpStatus.UNAUTHORIZED, "예기치 못한 에러가 발생하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
