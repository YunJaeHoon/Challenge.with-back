package Challenge.with_back.common.entity.redis;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@RedisHash(value = "check_verification_code", timeToLive = 60 * 60)
public class CheckVerificationCode
{
    // 이메일
    @Id
    private String email;

    @Builder
    public CheckVerificationCode(String email) {
        this.email = email;
    }
}
