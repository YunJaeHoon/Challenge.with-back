package Challenge.with_back.security.handler;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.dto.response.CustomSuccessCode;
import Challenge.with_back.dto.response.ExceptionResponseDto;
import Challenge.with_back.dto.response.SuccessResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler
{
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ExceptionResponseDto responseDto = ExceptionResponseDto.builder().build();

        if (exception instanceof BadCredentialsException || exception instanceof InternalAuthenticationServiceException) {
            responseDto.setCode("WRONG_PASSWORD");
            responseDto.setMessage("이메일 또는 비밀번호가 틀렸습니다.");
        } else if (exception instanceof DisabledException) {
            responseDto.setCode("DISABLED_ACCOUNT");
            responseDto.setMessage("비활성화 된 계정입니다.");
        } else {
            responseDto.setCode("UNEXPECTED_ERROR");
            responseDto.setMessage("예기치 못한 에러가 발생하였습니다.");
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
