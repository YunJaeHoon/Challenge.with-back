package Challenge.with_back.common.security.exception;

import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.response.ExceptionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException
    {
        ExceptionResponseDto responseDto = ExceptionResponseDto.builder()
                .code(CustomExceptionCode.NOT_LOGIN.name())
                .message(CustomExceptionCode.NOT_LOGIN.getMessage())
                .data(null)
                .build();

        response.setStatus(CustomExceptionCode.NOT_LOGIN.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
