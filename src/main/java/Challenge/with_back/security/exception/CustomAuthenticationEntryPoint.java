package Challenge.with_back.security.exception;

import Challenge.with_back.common.response.exception.ExceptionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        ExceptionResponseDto responseDto = ExceptionResponseDto.builder()
                .code("NOT_LOGIN")
                .message("로그인이 필요합니다.")
                .data(null)
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
