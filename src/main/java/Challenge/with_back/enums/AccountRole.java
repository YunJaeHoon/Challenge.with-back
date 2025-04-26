package Challenge.with_back.enums;


import lombok.AllArgsConstructor;

// 계정 권한 enum
@AllArgsConstructor
public enum AccountRole
{
    ADMIN("관리자 권한"),
    USER("일반 사용자 권한");

    private final String description;
}
