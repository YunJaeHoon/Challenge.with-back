package Challenge.with_back.security.exception;

import Challenge.with_back.dto.response.ExceptionResponseDto;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.dto.response.CustomExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler
{
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        ExceptionResponseDto responseDto = ExceptionResponseDto.builder()
                .code("LOW_AUTHORITY")
                .message("권한이 부족합니다.")
                .data(null)
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
