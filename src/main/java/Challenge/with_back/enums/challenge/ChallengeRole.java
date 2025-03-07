package Challenge.with_back.enums.challenge;

import lombok.AllArgsConstructor;

// 챌린지 역할 enum
@AllArgsConstructor
public enum ChallengeRole
{
    SUPER_ADMIN("최고 관리자 역할"),
    ADMIN("관리자 역할"),
    USER("일반 사용자 역할");

    private final String description;
}
