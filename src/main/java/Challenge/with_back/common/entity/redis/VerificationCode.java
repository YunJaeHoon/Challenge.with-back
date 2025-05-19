package Challenge.with_back.common.entity.redis;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@RedisHash(value = "verification_code", timeToLive = 60 * 10)
public class VerificationCode
{
    // 이메일
    @Id
    private String email;

    // 인증번호
    private String code;

    // 틀린 개수
    private int countWrong;

    // 틀린 개수 증가
    public void increaseCountWrong() {
        this.countWrong++;
    }
}
