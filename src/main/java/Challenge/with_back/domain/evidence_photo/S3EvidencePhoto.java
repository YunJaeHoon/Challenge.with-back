package Challenge.with_back.domain.evidence_photo;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class S3EvidencePhoto
{
    // 증거사진 URL
    private String photoUrl;

    // S3 이미지 파일 이름
    private String filename;
}
