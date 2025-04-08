package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;

// 알림 타입 enum
@AllArgsConstructor
public enum NotificationType
{
    WELCOME("회원가입 환영 알림");

    private final String description;
}
