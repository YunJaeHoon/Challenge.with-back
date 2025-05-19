package Challenge.with_back.common.entity.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@RedisHash(value = "check_verification_code", timeToLive = 60 * 60)
public class CheckVerificationCode
{
    // 이메일
    @Id
    private String email;
}
