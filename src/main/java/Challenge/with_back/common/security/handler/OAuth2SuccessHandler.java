package Challenge.with_back.common.security.handler;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.common.security.jwt.JwtUtil;
import Challenge.with_back.common.security.jwt.Token;
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
        String accessToken = jwtUtil.getToken(user.getId(), Token.ACCESS_TOKEN);
        String refreshToken = jwtUtil.getToken(user.getId(), Token.REFRESH_TOKEN);

        // Access token 쿠키 생성
        Cookie accessTokenCookie = jwtUtil.parseTokenToCookie(accessToken, Token.ACCESS_TOKEN);

        // Refresh token 쿠키 생성
        Cookie refreshTokenCookie = jwtUtil.parseTokenToCookie(refreshToken, Token.REFRESH_TOKEN);

        // 쿠키 저장
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // 리다이렉트
        response.sendRedirect(frontendURL + "/oauth2-callback");
    }
}
