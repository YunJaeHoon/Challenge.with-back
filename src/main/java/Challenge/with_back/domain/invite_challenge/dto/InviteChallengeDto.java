package Challenge.with_back.domain.invite_challenge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class InviteChallengeDto
{
    @NotNull(message = "챌린지 ID 리스트를 입력해주세요.")
    private Long challengeId;

    @NotNull(message = "초대할 사용자 ID 리스트를 입력해주세요.")
    private List<Long> inviteUserIdList;
}
