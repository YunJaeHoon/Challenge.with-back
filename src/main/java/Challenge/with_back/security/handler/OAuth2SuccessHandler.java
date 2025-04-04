package Challenge.with_back.security.handler;

import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.security.CustomUserDetails;
import Challenge.with_back.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler
{
    private final JwtUtil jwtUtil;

    @Value("${FRONTEND_URL}")
    private String frontendURL;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        // 계정 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 토큰 생성
        String accessToken = jwtUtil.getToken(user.getId(), true);
        String refreshToken = jwtUtil.getToken(user.getId(), false);

        // Access token 쿠키 생성
        Cookie accessTokenCookie = jwtUtil.parseTokenToCookie(accessToken, true);

        // Refresh token 쿠키 생성
        Cookie refreshTokenCookie = jwtUtil.parseTokenToCookie(refreshToken, false);

        // 쿠키 저장
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // 리다이렉트
        response.sendRedirect(frontendURL + "/oauth2-callback");
    }
}
