package Challenge.with_back.domain.challenge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class JoinChallengeDto
{
    @NotNull(message = "챌린지 ID를 입력해주세요.")
    private Long challengeId;
}
