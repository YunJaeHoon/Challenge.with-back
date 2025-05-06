package Challenge.with_back.domain.challenge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EvidencePhotoDto
{
    private Long evidencePhotoId;
    private String url;
}
