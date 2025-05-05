package Challenge.with_back.common.security.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccessTokenDto
{
    private String accessToken;     // Access token
}
