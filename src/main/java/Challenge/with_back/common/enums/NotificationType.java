package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;

// 알림 타입 enum
@AllArgsConstructor
public enum NotificationType
{
    TEST("테스트 알림"),
    WELCOME("회원가입 환영 알림"),
    FRIEND_REQUEST("친구 요청"),
    INVITE_CHALLENGE("챌린지 초대"),;

    private final String description;
}
