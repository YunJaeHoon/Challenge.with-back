package Challenge.with_back.dto.token;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenDto
{
    private String accessToken;     // Access token
    private String refreshToken;    // Refresh token
}
