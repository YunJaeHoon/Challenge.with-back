package Challenge.with_back.security.handler;

import Challenge.with_back.dto.response.CustomSuccessCode;
import Challenge.with_back.dto.response.SuccessResponseDto;
import Challenge.with_back.dto.token.TokenDto;
import Challenge.with_back.entity.User;
import Challenge.with_back.security.CustomUserDetails;
import Challenge.with_back.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler
{
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        ObjectMapper objectMapper = new ObjectMapper();;

        // 계정 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // remember-me 값 추출
        String rememberMeParam = request.getParameter("remember-me");
        boolean rememberMe;

        if(rememberMeParam == null)
            rememberMe = true;
        else
            rememberMe = rememberMeParam.equalsIgnoreCase("true");

        // 토큰 생성
        String accessToken = jwtUtil.getToken(user.getId(), true);
        String refreshToken = rememberMe ? jwtUtil.getToken(user.getId(), false) : null;

        TokenDto data = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        SuccessResponseDto responseDto = SuccessResponseDto.builder()
                .code(rememberMe ? "SUCCESS_REMEMBER" : "SUCCESS_FORGET")
                .data(data)
                .message("로그인을 성공하였습니다.")
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
