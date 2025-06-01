package Challenge.with_back.common.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // 로그인 관련 예외
    NOT_LOGIN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    LOW_AUTHORITY(HttpStatus.UNAUTHORIZED, "권한이 부족합니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 틀렸습니다."),
    DISABLED_ACCOUNT(HttpStatus.UNAUTHORIZED, "비활성화 된 계정입니다."),
    INVALID_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다."),

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
    TOO_MANY_PARTICIPATE_CHALLENGE(HttpStatus.BAD_REQUEST, "이미 최대 개수의 챌린지에 참여하고 있습니다."),
    IS_NOT_PREMIUM(HttpStatus.UNAUTHORIZED, "프리미엄 요금제를 구매해야 합니다."),

    // 인증번호 관련 예외
    SEND_EMAIL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 오류가 발생하였습니다."),
    VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "전송된 인증번호가 존재하지 않습니다."),
    CHECK_VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "인증번호를 확인받지 않았습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.GONE, "만료된 인증번호입니다."),
    WRONG_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    TOO_MANY_WRONG_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호를 5회 이상 틀렸습니다."),

    // 친구 관련 예외
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 데이터가 존재하지 않습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 요청 데이터가 존재하지 않습니다."),
    FRIEND_BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 차단 데이터가 존재하지 않습니다."),
    SAME_SENDER_AND_RECEIVER(HttpStatus.BAD_REQUEST, "본인에게 친구 요청을 보낼 수는 없습니다."),
    SAME_BLOCKING_USER_AND_BLOCKED_USER(HttpStatus.BAD_REQUEST, "본인을 차단할 수는 없습니다."),
    DIFFERENT_BLOCKING_USER_AND_REQUESTER(HttpStatus.BAD_REQUEST, "해당 친구 차단 데이터의 소유자가 아닙니다."),
    ALREADY_FRIEND(HttpStatus.CONFLICT, "이미 둘은 친구 사이입니다."),
    ALREADY_BLOCKED_FRIEND(HttpStatus.CONFLICT, "이미 차단한 사용자입니다."),

    // 알림 예외
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    NOTIFICATION_OWNERSHIP_INVALID(HttpStatus.FORBIDDEN, "해당 사용자의 알림이 아닙니다."),
    
    // 챌린지 예외
    PARTICIPATE_CHALLENGE_NOT_OWNED(HttpStatus.FORBIDDEN, "해당 사용자의 챌린지 참여 정보가 아닙니다."),
    PARTICIPATE_PHASE_NOT_OWNED(HttpStatus.FORBIDDEN, "해당 사용자의 페이즈 참여 정보가 아닙니다."),
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "챌린지가 존재하지 않습니다."),
    PHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "페이즈가 존재하지 않습니다."),
    PARTICIPATE_CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "챌린지 참여 정보가 존재하지 않습니다."),
    PARTICIPATE_PHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "페이즈 참여 정보가 존재하지 않습니다."),
    ALREADY_PARTICIPATING_CHALLENGE(HttpStatus.CONFLICT, "이미 참여 중인 챌린지입니다."),
    INVALID_CHALLENGE_ICON(HttpStatus.BAD_REQUEST, "유효하지 않은 챌린지 아이콘입니다."),
    INVALID_CHALLENGE_COLOR_THEME(HttpStatus.BAD_REQUEST, "유효하지 않은 챌린지 테마 색상입니다."),
    INVALID_CHALLENGE_NAME_FORMAT(HttpStatus.BAD_REQUEST, "형식에 맞지 않는 챌린지 이름입니다."),
    INVALID_CHALLENGE_DESCRIPTION_FORMAT(HttpStatus.BAD_REQUEST, "형식에 맞지 않는 챌린지 설명입니다."),
    INVALID_CHALLENGE_GOAL_COUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 챌린지 목표 개수입니다."),
    INVALID_CHALLENGE_UNIT(HttpStatus.BAD_REQUEST, "유효하지 않은 챌린지 단위입니다."),
    INVALID_PARTICIPATE_PHASE_COMMENT(HttpStatus.BAD_REQUEST, "형식에 맞지 않는 한마디입니다."),
    INVALID_PARTICIPATE_PHASE_CURRENT_COUNT(HttpStatus.BAD_REQUEST, "크기에 맞지 않는 달성 개수입니다."),
    INVALID_UPDATE_PARTICIPATE_PHASE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 페이즈 참여 정보 변경 타입입니다."),
    FULL_CHALLENGE(HttpStatus.BAD_REQUEST, "챌린지가 최대 인원수를 초과하였습니다."),
    PRIVATE_CHALLENGE(HttpStatus.FORBIDDEN, "비공개 챌린지입니다."),

    // S3 예외
    TOO_MANY_EVIDENCE_PHOTO(HttpStatus.BAD_REQUEST, "증거사진 최대 개수를 초과합니다."),
    FILE_IS_EMPTY(HttpStatus.NOT_FOUND, "파일이 비어있습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일이 아닙니다."),
    FILE_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "파일 이름이 존재하지 않습니다."),
    FILE_EXTENSION_NOT_FOUND(HttpStatus.NOT_FOUND, "파일 확장자가 존재하지 않습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 확장자입니다."),
    EVIDENCE_PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "증거사진이 존재하지 않습니다."),
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 이미지 업로드 중 오류가 발생하였습니다."),
    S3_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 이미지 삭제 중 오류가 발생하였습니다."),

    // Spring Batch 예외
    CREATE_PHASE_SCHEDULER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "페이즈 생성: 스프링 배치를 통한 스케줄러 작업 중 오류가 발생하였습니다."),
    DELETE_FRIEND_REQUEST_SCHEDULER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "친구 요청 데이터 삭제: 스프링 배치를 통한 스케줄러 작업 중 오류가 발생하였습니다."),

    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 에러가 발생하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
