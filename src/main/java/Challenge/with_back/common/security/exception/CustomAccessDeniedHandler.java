package Challenge.with_back.common.security.exception;

import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.common.response.exception.ExceptionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler
{
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException
    {
        ExceptionResponseDto responseDto = ExceptionResponseDto.builder()
                .code(CustomExceptionCode.LOW_AUTHORITY.name())
                .message(CustomExceptionCode.LOW_AUTHORITY.getMessage())
                .data(null)
                .build();

        response.setStatus(CustomExceptionCode.LOW_AUTHORITY.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
