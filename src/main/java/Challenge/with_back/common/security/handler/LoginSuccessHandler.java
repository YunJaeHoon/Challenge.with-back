package Challenge.with_back.common.security.handler;

import Challenge.with_back.common.response.CustomSuccessCode;
import Challenge.with_back.common.response.SuccessResponseDto;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.common.security.jwt.JwtUtil;
import Challenge.with_back.common.security.jwt.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler
{
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        // 계정 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // remember-me 값 추출
        String rememberMeParam = request.getParameter("remember-me");
        boolean rememberMe = "true".equalsIgnoreCase(rememberMeParam);

        // Access token 생성
        String accessToken = jwtUtil.getToken(user.getId(), Token.ACCESS_TOKEN);
        Cookie accessTokenCookie = jwtUtil.parseTokenToCookie(accessToken, Token.ACCESS_TOKEN);
        response.addCookie(accessTokenCookie);

        // Refresh token 생성
        if(rememberMe)
        {
            String refreshToken = jwtUtil.getToken(user.getId(), Token.REFRESH_TOKEN);
            Cookie refreshTokenCookie = jwtUtil.parseTokenToCookie(refreshToken, Token.REFRESH_TOKEN);
            response.addCookie(refreshTokenCookie);
        }

        SuccessResponseDto responseDto = SuccessResponseDto.builder()
                .code(rememberMe ? CustomSuccessCode.SUCCESS_REMEMBER.name() : CustomSuccessCode.SUCCESS_FORGET.name())
                .data(null)
                .message("로그인을 성공하였습니다.")
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
