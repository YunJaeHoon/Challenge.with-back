package Challenge.with_back.common.aop.aspect;

import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PremiumCheckAspect
{
    private final AccountUtil accountUtil;

    @Before("@annotation(Challenge.with_back.common.aop.annotation.PremiumOnly)")
    public void checkPremiumUser(JoinPoint joinPoint)
    {
        // 사용자 엔티티
        User user = accountUtil.getUserFromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        // 프리미엄 사용자가 아니라면 예외 처리
        if(!accountUtil.isPremium(user))
            throw new CustomException(CustomExceptionCode.IS_NOT_PREMIUM, null);
    }
}
