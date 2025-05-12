package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 페이즈 참여 정보 업데이트 타입 enum
@AllArgsConstructor
@Getter
public enum UpdateParticipatePhaseInfoType
{
    UPDATE_CURRENT_COUNT("달성 개수 변경"),
    UPDATE_COMMENT("한마디 변경"),
    TOGGLE_IS_EXEMPT("면제 여부 토글");

    private final String description;
}
