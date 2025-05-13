package Challenge.with_back.domain.challenge.dto;

import Challenge.with_back.common.enums.UpdateParticipatePhaseType;
import lombok.*;

@Getter
@Builder
public class UpdateParticipatePhaseMessage
{
    private String type;                // 업데이트 종류
    private Long userId;                // 사용자 ID
    private Long participatePhaseId;    // 페이즈 참여 정보 ID
    private Object data;                // 데이터
}
