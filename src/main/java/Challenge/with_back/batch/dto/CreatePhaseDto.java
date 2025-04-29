package Challenge.with_back.batch.dto;

import Challenge.with_back.entity.rdbms.Challenge;
import Challenge.with_back.entity.rdbms.ParticipatePhase;
import Challenge.with_back.entity.rdbms.Phase;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreatePhaseDto
{
    Challenge challenge;
    Phase phase;
    List<ParticipatePhase> participatePhases;
}
