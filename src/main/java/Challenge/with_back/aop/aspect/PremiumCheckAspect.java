package Challenge.with_back.aop.aspect;

import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.UserRepository;
import Challenge.with_back.response.exception.CustomException;
import Challenge.with_back.response.exception.CustomExceptionCode;
import Challenge.with_back.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Aspect
@Component
@RequiredArgsConstructor
public class PremiumCheckAspect
{
    private final AccountUtil accountUtil;

    @Before("@annotation(Challenge.with_back.aop.annotation.PremiumOnly)")
    public void checkPremiumUser(JoinPoint joinPoint)
    {
        // 사용자 인증 정보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자가 로그인하지 않은 상태라면 예외 처리
        if (authentication == null || !authentication.isAuthenticated())
            throw new CustomException(CustomExceptionCode.NOT_LOGIN, null);

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails))
            throw new CustomException(CustomExceptionCode.INVALID_AUTHENTICATION, null);

        // 사용자 객체
        User user = userDetails.getUser();

        // 프리미엄 사용자가 아니라면 예외 처리
        if(!accountUtil.isPremium(user))
            throw new CustomException(CustomExceptionCode.IS_NOT_PREMIUM, null);
    }
}
